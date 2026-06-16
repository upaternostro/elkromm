package org.paternostro.elkromm.impl;

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

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            os.write(preparePacket(this.plantCode, ElkronCommand.HELLO));
            Thread.sleep(DELAY);
            if (getNextDeescapedByte(is) != BYTE_ACK) throw new AssertionError("No ACK received");

            List<Byte>  plantCodeBytes = bcd(plantCode, 4);
            List<Byte>  technicalCodeBytes = bcd(technicalCode, 3);

            plantCodeBytes.addAll(technicalCodeBytes);

            byte[] loginData = listToArray(plantCodeBytes);

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.LOGIN, loginData));
            Thread.sleep(DELAY);
            if (getNextDeescapedByte(is) != BYTE_ACK) throw new AssertionError("No ACK received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
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
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
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
        byte[]  data = getData(ElkronCommand.PARTITIONS_AND_AREAS);

        return ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().deserialize(data);
    }

    @Override
    public boolean[] getSystemStatus()
    {
        boolean[]   retval = new boolean[MAX_PARTITIONS];
        byte[]      data = getData(ElkronCommand.SYSTEM_STATUS);

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
            os.write(preparePacket(this.plantCode, ElkronCommand.ARM_DISARM_SECTOR, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
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
        byte[]                          data = getData(ElkronCommand.INPUT_STATUS);

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
            os.write(preparePacket(this.plantCode, ElkronCommand.EXCLUDE_INCLUDE_INPUT, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
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
        byte[]  data = getData(ElkronCommand.PERIPHERAL_UNITS_ADDRESSES);

        return ElkrommFactory.getFactory().getPeripheralUnitsSerializer().deserialize(data);
    }

    @Override
    public Checksums getChecksums()
    {
        byte[]  data = getData(ElkronCommand.CHECKSUM);
        
        return ElkrommFactory.getFactory().getChecksumsSerializer().deserialize(data);
    }

    @Override
    public Credential[] getUsers()
    {
        byte[]  data = getData(ElkronCommand.USERS);

        return ElkrommFactory.getFactory().getUsersSerializer().deserialize(data);
    }

    @Override
    public Credential[] getKeys()
    {
        byte[]      data = getData(ElkronCommand.KEYS);

        return ElkrommFactory.getFactory().getKeysSerializer().deserialize(data);
    }

    @Override
    public Expansion[] getExpansions()
    {
        byte[]          data = getData(ElkronCommand.EXPANSIONS);

        return ElkrommFactory.getFactory().getExpansionsSerializer().deserialize(data);
    }

    @Override
    public boolean[] getUserEnablings()
    {
        boolean[]   retval = new boolean[MAX_CREDENTIALS];
        byte[]      data = getData(ElkronCommand.USER_ENABLINGS);
        int         enablings = ElkrommUtils.getLong(data, 0);

        for (int i = MAX_CREDENTIALS - 1; i >= 0; i--) {
            retval[MAX_CREDENTIALS - 1 - i] = (enablings & (1 << i)) != 0x00;
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
            os.write(preparePacket(this.plantCode, ElkronCommand.ENABLE_DISABLE_USER, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
        }
    }

    private byte[] getData(ElkronCommand cmd)
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
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
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
            os.write(preparePacket(this.plantCode, ElkronCommand.LOGOUT));
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
    private byte[] preparePacket(int plantCode, ElkronCommand command)
    {
        return preparePacket(plantCode, command, null, (byte)0, (byte)0);
    }

    // single packet
    private byte[] preparePacket(int plantCode, ElkronCommand command, byte[] data)
    {
        return preparePacket(plantCode, command, data, (byte)0, (byte)0);
    }

    // generic packet
    private byte[] preparePacket(int plantCode, ElkronCommand command, byte[] data, byte packets, byte index)
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
        retval[7] = (byte)(command.getValue() & 0xFF);

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
                os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
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
