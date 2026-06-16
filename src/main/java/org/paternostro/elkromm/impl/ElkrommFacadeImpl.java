package org.paternostro.elkromm.impl;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Output;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElkrommFacadeImpl implements ElkrommFacade
{
    public static final Logger logger = LoggerFactory.getLogger(ElkrommFacadeImpl.class);

    public static final int DELAY = 100;

    private InetAddress inetAddr;
    private int port;
    private int plantCode;
    private Status  status;
    private Socket s;
    private InputStream is;
    private OutputStream os;

    public ElkrommFacadeImpl()
    {
        this.status = Status.ST_NOT_INITIALIZED;
    }

    public void init(InetAddress inetAddr, int port, int plantCode)
    {
        if (status != Status.ST_NOT_INITIALIZED) throw new AssertionError("Wrong status");
        
        this.inetAddr = inetAddr;
        this.port = port;
        this.plantCode = plantCode;
        this.status = Status.ST_DISCONNECTED;
    }

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public void connect()
    {
        if (status != Status.ST_DISCONNECTED) throw new AssertionError("Wrong status");

        try {
            s = new Socket(inetAddr, port);
            is = s.getInputStream();
            os = s.getOutputStream();
            status = Status.ST_CONNECTED;
        } catch (IOException e) {
            logger.error("Communication error", e);
        }
    }

    @Override
    public void disconnect()
    {
        if (status == Status.ST_DISCONNECTED) return;

        try {
            status = Status.ST_DISCONNECTED;
            os.close();
            is.close();
            s.close();
        } catch (IOException e) {
            logger.error("Communication error", e);
        }
    }

    @Override
    public void login(int plantCode, int technicalCode)
    {
        if (status == Status.ST_LOGGED_IN) return;
        if (status != Status.ST_CONNECTED) throw new AssertionError("Wrong status");

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_HELLO));
            Thread.sleep(DELAY);
            if (getNextDeescapedByte(is) != BYTE_ACK) throw new AssertionError("No ACK received");

            List<Byte>  plantCodeBytes = bcd(plantCode, 4);
            List<Byte>  technicalCodeBytes = bcd(technicalCode, 3);

            plantCodeBytes.addAll(technicalCodeBytes);

            byte[] loginData = listToArray(plantCodeBytes);

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_LOGIN, loginData));
            Thread.sleep(DELAY);
            if (getNextDeescapedByte(is) != BYTE_ACK) throw new AssertionError("No ACK received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_SEND));
            Thread.sleep(DELAY);
            if (getNextDeescapedByte(is) != BYTE_SYN) throw new AssertionError("No SYN received");

            status = Status.ST_LOGGED_IN;
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    @Override
    public void ping()
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_SEND));
            Thread.sleep(DELAY);
            if (getNextDeescapedByte(is) != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    @Override
    public AreasAndPartitions getAreasAndPartitions()
    {
        AreasAndPartitions  retval = new AreasAndPartitions();
        byte[]              data = getData(CMD_PARTITIONS_AND_AREAS);
        int                 areas;
        int                 partitions;
        int                 selfExclusion;
        int                 armingBlock;

        areas = data[0]; // numero di aree

        for (byte i = 0; i < areas; i++) {
            retval.addArea(new Area(i, ElkrommUtils.getText(data, 5 + i * 24, 24), ElkrommUtils.unpackPartitions(data[i + 1])));
        }

        partitions = data[101]; // numero di settori
        selfExclusion = data[102];
        armingBlock = data[103];

        for (byte i = 0; i < partitions; i++) {
            String name = ElkrommUtils.getText(data, 136 + i * 24, 24);
            org.paternostro.elkromm.dto.Partition.Type type;
            byte partitionBitMask = Partition.values()[i].getBitMask();

            if ((selfExclusion & partitionBitMask) == 0x00) {
                if ((armingBlock & partitionBitMask) == 0x00) {
                    type = org.paternostro.elkromm.dto.Partition.Type.STANDARD;
                } else {
                    type = org.paternostro.elkromm.dto.Partition.Type.ARMING_BLOCK;
                }
            } else {
                if ((armingBlock & partitionBitMask) == 0x00) {
                    type = org.paternostro.elkromm.dto.Partition.Type.SELF_EXCLUSION;
                } else {
                    type = org.paternostro.elkromm.dto.Partition.Type.UNKNOWN;
                }
            }

            retval.addPartition(new org.paternostro.elkromm.dto.Partition(i, name, false, type, ElkrommUtils.getWord(data, 104 + i * 2), ElkrommUtils.getWord(data, 120 + i * 2)));
        }

        return retval;
    }

    @Override
    public boolean[] getSystemStatus()
    {
        boolean[]   retval = new boolean[MAX_PARTITIONS];
        byte[]      data = getData(CMD_SYSTEM_STATUS);

        for (Partition partition : Partition.values()) {
            retval[partition.ordinal()] = (data[0] & partition.getBitMask()) != 0x00;
        }

        return retval;
    }

    @Override
    public void armDisarmSector(Partition partition, boolean arm)
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        byte[] data = new byte[2];

        data[0] = partition.getBitMask();
        data[1] = arm ? partition.getBitMask() : 0x00;

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_ARM_DISARM_SECTOR, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_SEND));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    @Override
    public Map<InputStatus,List<Integer>> getInputStatus()
    {
        Map<InputStatus,List<Integer>>  retval = new HashMap<>();
        byte[]                          data = getData(CMD_INPUT_STATUS);

        for (InputStatus status : InputStatus.values()) {
            List<Integer>   inputs = new ArrayList<>();

            for (int i = 0; i < data.length; i++) {
                if (status == ElkrommFacade.InputStatus.IS_ALL ||
                        (status == ElkrommFacade.InputStatus.IS_CLOSED && (data[i] & ElkrommFacade.InputStatus.IS_OPEN.getBitMask()) == 0) ||
                        (data[i] & status.getBitMask()) != 0) {
                    inputs.add(i);
                }
            }

            if (inputs.size() > 0) {
                retval.put(status, inputs);
            }
        }

        return retval;
    }

    @Override
    public void excludeIncludeInput(byte inputOrdinal, boolean exclude)
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        byte[] data = new byte[2];

        data[0] = inputOrdinal;
        data[1] = (byte)(exclude ? 0x01 : 0x00);

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_EXCLUDE_INCLUDE_INPUT, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_SEND));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    @Override
    public PeripheralUnits getPeripheralUnitsAddresses()
    {
        PeripheralUnits retval = new PeripheralUnits();
        byte[]          data = getData(CMD_PERIPHERAL_UNITS_ADDRESSES);
        int             offset = 0;
        int             length = data[offset++];

        for (int i = 0; i < length; i++) {
            retval.addKeypad(data[offset++]);
        }

        length = data[offset++];

        for (int i = 0; i < length; i++) {
            retval.addReader(data[offset++]);
        }

        length = data[offset++];

        for (int i = 0; i < length; i++) {
            retval.addExpansion(data[offset++]);
        }

        return retval;
    }

    @Override
    public Checksums getChecksums()
    {
        byte[]  data = getData(CMD_CHECKSUM);
        
        return new Checksums(
                ElkrommUtils.getLong(data,  0),
                ElkrommUtils.getLong(data,  4),
                ElkrommUtils.getLong(data,  8),
                ElkrommUtils.getLong(data, 12),
                ElkrommUtils.getLong(data, 16),
                ElkrommUtils.getLong(data, 20),
                ElkrommUtils.getLong(data, 24),
                ElkrommUtils.getLong(data, 28),
                ElkrommUtils.getLong(data, 32),
                ElkrommUtils.getLong(data, 36),
                ElkrommUtils.getLong(data, 40),
                ElkrommUtils.getLong(data, 44),
                ElkrommUtils.getLong(data, 48)
        );
    }

    @Override
    public User[] getUsers()
    {
        List<User>  retval = new ArrayList<>();
        byte[]      data = getData(CMD_USERS);

        for (int i = 0; i < 32; i++) {
           retval.add(new User((data[26 * i] & 0x02) != 0x00, ElkrommUtils.unpackPartitions(data[26 * i + 1]),  ElkrommUtils.getText(data, 26 * i + 2, 24)));
        }

        return retval.toArray(new User[0]);
    }

    @Override
    public Key[] getKeys()
    {
        List<Key>  retval = new ArrayList<>();
        byte[]      data = getData(CMD_KEYS);

        for (int i = 0; i < 32; i++) {
            // FIXME: byte[0] cos'e'? always enabled come users?
            retval.add(new Key(ElkrommUtils.unpackPartitions(data[26 * i + 1]),  ElkrommUtils.getText(data, 26 * i + 2, 24)));
        }

        return retval.toArray(new Key[0]);
    }

    @Override
    public Expansion[] getExpansions()
    {
        List<Expansion> retval = new ArrayList<>();
        byte[]          data = getData(CMD_EXPANSIONS);

        for (int i = 0; i < data.length / 559; i++) {
            int         offset = i * 559;
            Expansion   expansion = new Expansion(data[offset + 1], ElkrommUtils.getText(data, offset + 3, 4), ElkrommUtils.getText(data, offset + 533, 24));

            for (int j = 0; j < 8; j++) {
                offset = i * 559 + j * 38 + 7;

                if (data[offset] == 0x00) {
                    // Unused input, skip
                    continue;
                }

                Input.Configuration     configuration = null;
                Input.Specialization    specialization = null;

                for (Input.Configuration pivot : Input.Configuration.values()) {
                    if (pivot.getValue() == data[offset + 1]) {
                        configuration = pivot;
                        break;
                    }
                }

                for (Input.Specialization pivot : Input.Specialization.values()) {
                    if (pivot.getValue() == data[offset + 2]) {
                        specialization = pivot;
                        break;
                    }
                }

                expansion.addInput(new Input(data[offset], configuration, specialization, ElkrommUtils.unpackPartitions(data[offset + 5]), ElkrommUtils.getText(data, offset + 6, 24)));
            }

            for (int j = 0; j < 6; j++) {
                offset = i * 559 + 8 * 38 + j * 37 + 7;

                if (data[offset] == 0x00) {
                    // Unused output, skip
                    continue;
                }

                Output.Type             type = null;
                Output.Specialization   specialization = null;

                for (Output.Type pivot : Output.Type.values()) {
                    if (pivot.getValue() == data[offset + 1]) {
                        type = pivot;
                        break;
                    }
                }

                for (Output.Specialization pivot : Output.Specialization.values()) {
                    if (pivot.getValue() == data[offset + 3]) {
                        specialization = pivot;
                        break;
                    }
                }

                expansion.addOutput(new Output(data[offset], type, ElkrommUtils.unpackPartitions(data[offset + 2]), specialization, ElkrommUtils.getText(data, offset + 8, 24)));
            }

            retval.add(expansion);

//            int checksum = 0;
//            for (int j = 0; j < 557; j++) {
//                checksum += data[i * 559 + j];
//            }
//            checksum += ElkrommUtils.getWord(data, i * 559 + 557);
//            logger.info("Checksum: " + checksum);
        }

        return retval.toArray(new Expansion[0]);
    }

    @Override
    public boolean[] getUserEnablings()
    {
        boolean[]   retval = new boolean[MAX_USERS];
        byte[]      data = getData(CMD_USER_ENABLINGS);
        int         enablings = ElkrommUtils.getLong(data, 0);

        for (int i = MAX_USERS - 1; i >= 0; i--) {
            retval[MAX_USERS - 1 - i] = (enablings & (1 << i)) != 0x00;
        }

        return retval;
    }

    @Override
    public void enableDisableUser(byte userOrdinal, boolean enable)
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        byte[] data = new byte[2];

        data[0] = userOrdinal;
        data[1] = (byte)(enable ? 0x01 : 0x00);

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_ENABLE_DISABLE_USER, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_SEND));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    private byte[] getData(byte cmd)
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        byte[] data = null;

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, cmd));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
            data = readPacket(this.plantCode, is, os);

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_SEND));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }

        return data;
    }

    public void logout()
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, CMD_LOGOUT));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            status = Status.ST_CONNECTED;
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    private List<Byte> bcd(int in, int minLen)
    {
        List<Byte> retval = new ArrayList<>();

        while (in > 0) {
            retval.add(0, bcdByte(in % 100));
            in /= 100;
        }

        while (retval.size() < minLen) {
            retval.add(0, (byte)0x00);
        }

        return retval;
    }

    private byte bcdByte(int in)
    {
        int lower = in % 10;
        in /= 10;
        int upper = in % 10;

        return (byte)(upper << 4 | lower);
    }

    private byte[] listToArray(List<Byte> list)
    {
        byte[] retval = new byte[list.size()];

        for (int i = 0; i < retval.length; i++) {
            retval[i] = list.get(i);
        }

        return retval;
    }

    // dataless packet
    private byte[] preparePacket(int plantCode, byte command)
    {
        return preparePacket(plantCode, command, null, (byte)0, (byte)0);
    }

    // single packet
    private byte[] preparePacket(int plantCode, byte command, byte[] data)
    {
        return preparePacket(plantCode, command, data, (byte)0, (byte)0);
    }

    // generic packet
    private byte[] preparePacket(int plantCode, byte command, byte[] data, byte packets, byte index)
    {
        assert(index <= packets);

        int dataLength = data == null ? 0 : data.length;
        List<Byte> bcdPlantCode = bcd(plantCode, 4);

        assert(dataLength <= MAX_DATA_LENGTH);

        byte[]  retval = new byte[dataLength + 11];

        // Setup header
        retval[0] = BYTE_SOH;
        retval[1] = bcdPlantCode.get(0);
        retval[2] = bcdPlantCode.get(1);
        retval[3] = packets;
        retval[4] = index;
        retval[5] = (byte)(dataLength & 0xFF);
        retval[6] = 0x00;
        retval[7] = command;

        // Copy data if any
        if (dataLength > 0) System.arraycopy(data, 0, retval, 8, dataLength);

        // Compute checksum
        int checksum = 0x10000;
        
        for (byte i = 1; i < dataLength + 8; i++) {
            checksum -= retval[i] & 0xFF;
        }

        retval[dataLength + 8] = (byte)((checksum & 0xFF00) >> 8);
        retval[dataLength + 9] = (byte)(checksum & 0x00FF);

        // Escape 0x01 and 0x03
        int escape = 0;

        for (byte i = 1; i < retval.length - 1; i++) {
            if (retval[i] == BYTE_SOH || retval[i] == BYTE_ETX) {
                escape++;
            }
        }

        if (escape != 0) {
            byte[]  temp = new byte[retval.length + escape];

            temp[0] = retval[0];

            for (byte i = 1, j = 1; i < retval.length - 1; i++, j++) {
                if (retval[i] == BYTE_SOH || retval[i] == BYTE_ETX) {
                    temp[j++] = BYTE_DC1;
                }

                temp[j] = retval[i];
            }

            retval = temp;
        }

        // Footer
        retval[retval.length - 1] = BYTE_ETX;

        return retval;
    }

    private byte[] readPacket(int plantCode, InputStream is, OutputStream os) throws IOException, InterruptedException
    {
        int pktNum = 0xff; // total number of packets (minus one)
        int pktOrdinal = -1; // current packet index
        byte[] retval = null;
        List<Byte> bcdPlantCode = bcd(plantCode, 4);

        while (pktOrdinal < pktNum) {
            if (pktOrdinal >= 0) {
                // not first time
                Thread.sleep(DELAY);
                os.write(preparePacket(this.plantCode, CMD_SEND));
                Thread.sleep(DELAY);
                if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
            }

            int c = is.read();

            if (c != BYTE_SOH) throw new AssertionError("No SOH received");

            int checksum = 0;

            c = getNextDeescapedByte(is);
            if (c != bcdPlantCode.get(0)) throw new AssertionError("Wrong packet[1]: expected " + bcdPlantCode.get(0) + " but got " + c);
            checksum += c;

            c = getNextDeescapedByte(is);
            if (c != bcdPlantCode.get(1)) throw new AssertionError("Wrong packet[2]: expected " + bcdPlantCode.get(1) + " but got " + c);
            checksum += c;

            pktNum = getNextDeescapedByte(is); // total number of packets (minus one)
            checksum += pktNum;

            pktOrdinal = getNextDeescapedByte(is); // current packet index
            checksum += pktOrdinal;

            int dataLen = getNextDeescapedByte(is); // number of data bytes
            checksum += dataLen;

            c = getNextDeescapedByte(is);
            if (c != 0x00) throw new AssertionError("Wrong packet[6]");
            checksum += c;

            int cmd = getNextDeescapedByte(is);
            checksum += cmd;

            int destOffset = 0;

            if (pktOrdinal == 0) {
                // First time
                destOffset = 0;
                retval = new byte[dataLen];
            } else {
                destOffset = retval.length;
                retval = Arrays.copyOf(retval, retval.length + dataLen);
            }

            while (dataLen > 0) {
                c = getNextDeescapedByte(is);

                retval[destOffset++] = (byte) c;
                checksum += c;
                dataLen--;
            }

            c = getNextDeescapedByte(is);
            checksum += c << 8;

            c = getNextDeescapedByte(is);
            checksum += c;

            if (checksum != 0x10000) throw new AssertionError("Wrong checksum");

            c = is.read();
            if (c != BYTE_ETX) throw new AssertionError("Wrong packet[last]");
        }

        return retval;
    }

    private int getNextDeescapedByte(InputStream is) throws IOException
    {
        int retval = is.read();

        if (retval == BYTE_DC1) {
            retval = is.read();
        }

        return retval;
    }
}
