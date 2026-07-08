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

import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.EnableDisableUser;
import org.paternostro.elkromm.dto.ExcludeIncludeInput;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.PartitionArming;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
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
    public void connect() throws ElkrommException
    {
        if (status != Status.ST_DISCONNECTED) throw new AssertionError("Wrong status");

        try {
            s = new Socket(inetAddr, port);
            is = s.getInputStream();
            os = s.getOutputStream();
            status = Status.ST_CONNECTED;
        } catch (IOException e) {
            logger.error("Communication error", e);
            throw new ElkrommException("Communication error", e);
        }
    }

    @Override
    public void disconnect() throws ElkrommException
    {
        if (status == Status.ST_DISCONNECTED) return;

        try {
            status = Status.ST_DISCONNECTED;
            os.close();
            is.close();
            s.close();
        } catch (IOException e) {
            logger.error("Communication error", e);
            throw new ElkrommException("Communication error", e);
        }
    }

    @Override
    public void login(int plantCode, int technicalCode) throws ElkrommException
    {
        if (status == Status.ST_LOGGED_IN) return;
        if (status != Status.ST_CONNECTED) throw new AssertionError("Wrong status");

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.HELLO));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_ACK) throw new AssertionError("No ACK received");

            List<Byte>  plantCodeBytes = ElkrommUtils.bcd(plantCode, 4);
            List<Byte>  technicalCodeBytes = ElkrommUtils.bcd(technicalCode, 3);

            plantCodeBytes.addAll(technicalCodeBytes);

            byte[] loginData = listToArray(plantCodeBytes);

            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.LOGIN, loginData));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_ACK) throw new AssertionError("No ACK received");

            status = Status.ST_LOGGED_IN;

            ping();
        } catch (IOException e) {
            logger.error("Communication error", e);
            throw new ElkrommException("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
            throw new ElkrommException("Interruption error", e);
        }
    }

    @Override
    public void ping() throws ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, ElkronCommand.SEND));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
        } catch (IOException e) {
            logger.error("Communication error", e);
            throw new ElkrommException("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
            throw new ElkrommException("Interruption error", e);
        }
    }

    @Override
    public AreasAndPartitions getAreasAndPartitions() throws ElkrommException
    {
        byte[]  data = getData(ElkronCommand.PARTITIONS_AND_AREAS);

        return ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().deserialize(data);
    }

    @Override
    public SystemStatus getSystemStatus() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.SYSTEM_STATUS);

        return ElkrommFactory.getFactory().getSystemStatusSerializer().deserialize(data);
    }

    @Override
    public void armDisarmSector(Partition partition, boolean arm) throws ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        PartitionArming pa = new PartitionArming(partition.getValue(), arm ? partition.getValue() : 0x00);
        byte[]          data = ElkrommFactory.getFactory().getPartitionArmingSerializer().serialize(pa);

        sendCommand(ElkronCommand.ARM_DISARM_SECTOR, data);
    }

    @Override
    public void armDisarmSectors(byte partitions, byte armingMask) throws ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        PartitionArming pa = new PartitionArming(partitions, armingMask);
        byte[]          data = ElkrommFactory.getFactory().getPartitionArmingSerializer().serialize(pa);

        sendCommand(ElkronCommand.ARM_DISARM_SECTOR, data);
    }

    private void sendCommand(ElkronCommand command, byte[] data) throws AssertionError, ElkrommException
    {
        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, command, data));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            ping();
        } catch (IOException e) {
            logger.error("Communication error", e);
            throw new ElkrommException("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
            throw new ElkrommException("Interruption error", e);
        }
    }

    @Override
    public Map<InputStatus,List<Integer>> getInputStatus() throws ElkrommException
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
    public void excludeIncludeInput(byte inputOrdinal, boolean exclude) throws ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        ExcludeIncludeInput eii = new ExcludeIncludeInput(inputOrdinal, exclude);
        byte[]              data = ElkrommFactory.getFactory().getExcludeIncludeInputSerializer().serialize(eii);

        sendCommand(ElkronCommand.EXCLUDE_INCLUDE_INPUT, data);
    }

    @Override
    public PeripheralUnits getPeripheralUnitsAddresses() throws ElkrommException
    {
        byte[]  data = getData(ElkronCommand.PERIPHERAL_UNITS_ADDRESSES);

        return ElkrommFactory.getFactory().getPeripheralUnitsSerializer().deserialize(data);
    }

    @Override
    public Checksums getChecksums() throws ElkrommException
    {
        byte[]  data = getData(ElkronCommand.CHECKSUM);
        
        return ElkrommFactory.getFactory().getChecksumsSerializer().deserialize(data);
    }

    @Override
    public Credential[] getUsers() throws ElkrommException
    {
        byte[]  data = getData(ElkronCommand.USERS);

        return ElkrommFactory.getFactory().getUsersSerializer().deserialize(data);
    }

    @Override
    public Credential[] getKeys() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.KEYS);

        return ElkrommFactory.getFactory().getKeysSerializer().deserialize(data);
    }

    @Override
    public Expansion[] getExpansions() throws ElkrommException
    {
        byte[]          data = getData(ElkronCommand.EXPANSIONS);

        return ElkrommFactory.getFactory().getExpansionsSerializer().deserialize(data);
    }

    @Override
    public boolean[] getUserEnablings() throws ElkrommException
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
    public void enableDisableUser(byte userOrdinal, boolean enable) throws ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        EnableDisableUser   edu = new EnableDisableUser(userOrdinal, enable);
        byte[]              data = ElkrommFactory.getFactory().getEnableDisableUserSerializer().serialize(edu);

        sendCommand(ElkronCommand.ENABLE_DISABLE_USER, data);
    }

    private byte[] getData(ElkronCommand cmd) throws ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        byte[] data = null;

        try {
            Thread.sleep(DELAY);
            os.write(preparePacket(this.plantCode, cmd));
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");
            data = readPacket(this.plantCode, is, os);

            ping();
        } catch (IOException e) {
            logger.error("Communication error", e);
            throw new ElkrommException("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
            throw new ElkrommException("Interruption error", e);
        }

        return data;
    }

    public void logout() throws ElkrommException
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
            throw new ElkrommException("Communication error", e);
        } catch (InterruptedException e) {
            logger.error("Interruption error", e);
            throw new ElkrommException("Interruption error", e);
        }
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
        List<Byte> bcdPlantCode = ElkrommUtils.bcd(plantCode, 4);

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
        List<Byte> bcdPlantCode = ElkrommUtils.bcd(plantCode, 4);

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

    @Override
    public Keyboard[] getKeyboards() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.KEYPADS);

        return ElkrommFactory.getFactory().getKeyboardsSerializer().deserialize(data);
    }

    @Override
    public ParametersEnablings getParametersEnablings() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.PARAMETERS_ENABLINGS);

        return ElkrommFactory.getFactory().getParametersEnablingsSerializer().deserialize(data);
    }

    @Override
    public PhoneNumbersSendingCodes getPhoneNumbersSendingCodes() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.PHONE_NUMBERS);

        return ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().deserialize(data);
    }

    @Override
    public PhoneParameters getPhoneParameters() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.PHONE_PARAMETERS);

        return ElkrommFactory.getFactory().getPhoneParametersSerializer().deserialize(data);
    }

    @Override
    public PSTNGSM getPSTNGSM() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.PSTN_GSM);

        return ElkrommFactory.getFactory().getPSTNGSMSerializer().deserialize(data);
    }

    @Override
    public Reader[] getReaders() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.READERS);

        return ElkrommFactory.getFactory().getReadersSerializer().deserialize(data);
    }

    @Override
    public SMSs getSMSs() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.SMS);

        return ElkrommFactory.getFactory().getSMSsSerializer().deserialize(data);
    }

    @Override
    public TimeProgrammer getTimeProgrammer() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.TIME_PROGRAMMER);

        return ElkrommFactory.getFactory().getTimeProgrammerSerializer().deserialize(data);
    }
}
