package org.paternostro.elkromm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.PacketQueue;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.DayClassCommands;
import org.paternostro.elkromm.dto.EnableDisableUser;
import org.paternostro.elkromm.dto.ExcludeIncludeInput;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.Login;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.PartitionArming;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SingleKeyboard;
import org.paternostro.elkromm.dto.SingleSMS;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
import org.paternostro.elkromm.packet.ElkrommPacket;
import org.paternostro.elkromm.packet.Hello;
import org.paternostro.elkromm.packet.Logout;
import org.paternostro.elkromm.packet.Send;
import org.paternostro.mock.ipc.Endpoint;
import org.paternostro.mock.ipc.EndpointFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkrommFacadeImpl implements ElkrommFacade
{
    public static final Logger logger = LoggerFactory.getLogger(ElkrommFacadeImpl.class);

    public static final int DELAY = 100;

    private Status  status;
    private PacketQueue packetQueue;
    private Map<ElkronCommand, SortedSet<ElkrommPacket>> cmdPayloads;
    private Endpoint endpoint;
    private List<Byte> bcdPlantCode;
    private InputStream is;
    private OutputStream os;

    public ElkrommFacadeImpl()
    {
        this.status = Status.ST_NOT_INITIALIZED;
        this.packetQueue = ElkrommFactory.getFactory().getPacketQueue();
        this.cmdPayloads = new HashMap<>();
    }

    @Override
    public void init(InetAddress inetAddr, int port, int plantCode) throws IOException
    {
        if (status != Status.ST_NOT_INITIALIZED) throw new AssertionError("Wrong status");
        
        this.endpoint = EndpointFactory.getFactory().getSocketEndpoint(inetAddr, port);
        this.status = Status.ST_DISCONNECTED;
        this.bcdPlantCode = ElkrommUtils.bcd(plantCode, 4);
    }

    @Override
    public void init(Endpoint endpoint, int plantCode)
    {
        if (status != Status.ST_NOT_INITIALIZED) throw new AssertionError("Wrong status");
        
        this.endpoint = endpoint;
        this.status = Status.ST_DISCONNECTED;
        this.bcdPlantCode = ElkrommUtils.bcd(plantCode, 4);
    }

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public void connect() throws ElkrommException
    {
        if (status == Status.ST_NOT_INITIALIZED) throw new AssertionError("Wrong status");
        if (status == Status.ST_LOGGED_IN) logout();
        if (status != Status.ST_DISCONNECTED) return;

        try {
            endpoint.connect();
            is = endpoint.getInputStream();
            os = endpoint.getOutputStream();
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
            // os.close();
            // is.close();
            endpoint.close();
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
            new Hello(bcdPlantCode.get(0), bcdPlantCode.get(1)).serialize(os);
            Thread.sleep(DELAY);
            if (is.read() != BYTE_ACK) throw new AssertionError("No ACK received");
            
            Thread.sleep(DELAY);
            new org.paternostro.elkromm.packet.Login(bcdPlantCode.get(0), bcdPlantCode.get(1), new Login(plantCode, technicalCode)).serialize(os);
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
            new Send(bcdPlantCode.get(0), bcdPlantCode.get(1)).serialize(os);
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
        byte[]  data = getData(ElkronCommand.SYSTEM_STATUS);

        return ElkrommFactory.getFactory().getSystemStatusSerializer().deserialize(data);
    }

    @Override
    public void armDisarmSector(Partition partition, boolean arm) throws ElkrommException
    {
        PartitionArming pa = new PartitionArming(partition.getValue(), arm ? partition.getValue() : 0x00);
        byte[]          data = ElkrommFactory.getFactory().getPartitionArmingSerializer().serialize(pa);

        sendCommand(ElkronCommand.ARM_DISARM_SECTOR, data);
    }

    @Override
    public void armDisarmSectors(byte partitions, byte armingMask) throws ElkrommException
    {
        PartitionArming pa = new PartitionArming(partitions, armingMask);
        byte[]          data = ElkrommFactory.getFactory().getPartitionArmingSerializer().serialize(pa);

        sendCommand(ElkronCommand.ARM_DISARM_SECTOR, data);
    }

    private void sendCommand(ElkronCommand command, byte[] data) throws AssertionError, ElkrommException
    {
        if (status != Status.ST_LOGGED_IN) throw new AssertionError("Wrong status");

        try {
            packetQueue.enqueuePayload(ElkrommPacket.packetFactoryAllocate(command, bcdPlantCode.get(0), bcdPlantCode.get(1), 0, 0, data == null ? 0 : data.length, data), data);

            while (!packetQueue.isEmpty()) {
                Thread.sleep(DELAY);
                packetQueue.remove().serialize(os);
                Thread.sleep(DELAY);
                if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

                ping();
            }
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
                        (status == ElkrommFacade.InputStatus.IS_CLOSED && data[i] == 0x00) ||
                        (data[i] & status.getBitMask()) != 0) {
                    inputs.add(i + 1);
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
        byte[]  data = getData(ElkronCommand.KEYS);

        return ElkrommFactory.getFactory().getKeysSerializer().deserialize(data);
    }

    @Override
    public Expansion[] getExpansions() throws ElkrommException
    {
        byte[]  data = getData(ElkronCommand.EXPANSIONS);

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
            ElkrommPacket.packetFactoryAllocate(cmd, bcdPlantCode.get(0), bcdPlantCode.get(1), 0, 0, 0, null).serialize(os);
            Thread.sleep(DELAY);
            if (is.read() != BYTE_SYN) throw new AssertionError("No SYN received");

            while (data == null) {
                data = ElkrommPacket.deserialize(is).cachePayload(cmdPayloads);
                ping();
            }
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
        if (status == Status.ST_NOT_INITIALIZED) throw new AssertionError("Wrong status");
        if (status != Status.ST_LOGGED_IN) return;

        try {
            Thread.sleep(DELAY);
            new Logout(bcdPlantCode.get(0), bcdPlantCode.get(1)).serialize(os);
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

    @Override
    public C200bParameters getC200bParameters() throws ElkrommException
    {
        byte[]      data = getData(ElkronCommand.C200B);

        return ElkrommFactory.getFactory().getC200bParametersSerializer().deserialize(data);
    }

    @Override
    public void setParametersEnablings(ParametersEnablings parametersEnablings) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_PARAMETERS_ENABLINGS, ElkrommFactory.getFactory().getParametersEnablingsSerializer().serialize(parametersEnablings));
    }

    @Override
    public void setAreasAndPartitions(AreasAndPartitions areasAndPartitions) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_PARTITIONS_AND_AREAS, ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().serialize(areasAndPartitions));
    }

    @Override
    public void setPhoneParameters(PhoneParameters phoneParameters) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_PHONE_PARAMETERS, ElkrommFactory.getFactory().getPhoneParametersSerializer().serialize(phoneParameters));
    }

    @Override
    public void setPhoneNumbersSendingCodes(PhoneNumbersSendingCodes phoneNumbersSendingCodes) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_PHONE_NUMBERS, ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().serialize(phoneNumbersSendingCodes));
    }

    @Override
    public void setC200bParameters(C200bParameters c200bParameters) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_C200B, ElkrommFactory.getFactory().getC200bParametersSerializer().serialize(c200bParameters));
    }

    @Override
    public void setSMSs(SMSs sMSs) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_SMS, ElkrommFactory.getFactory().getSMSsSerializer().serialize(sMSs));
    }

    @Override
    public void setPSTNGSM(PSTNGSM pSTNGSM) throws ElkrommException {
        sendCommand(ElkronCommand.SET_PSTN_GSM, ElkrommFactory.getFactory().getPSTNGSMSerializer().serialize(pSTNGSM));
    }

    @Override
    public void setUsers(Credential[] users) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_USERS, ElkrommFactory.getFactory().getUsersSerializer().serialize(users));
    }

    @Override
    public void setKeys(Credential[] keys) throws ElkrommException
    {
        sendCommand(ElkronCommand.SET_KEYS, ElkrommFactory.getFactory().getKeysSerializer().serialize(keys));
    }

    @Override
    public void setSMS(SingleSMS sMS) throws ElkrommException
    {
        sendCommand(ElkronCommand.SMS_PROGRAMMING, ElkrommFactory.getFactory().getSingleSMSSerializer().serialize(sMS));
    }

    @Override
    public void setKeyboard(SingleKeyboard keyboard) throws ElkrommException
    {
        sendCommand(ElkronCommand.KEYPAD_PROGRAMMING, ElkrommFactory.getFactory().getSingleKeyboardSerializer().serialize(keyboard));
    }

    @Override
    public void setReader(Reader reader) throws ElkrommException
    {
        // FIXME: da provare!?!?
        sendCommand(ElkronCommand.READER_PROGRAMMING, ElkrommFactory.getFactory().getReaderSerializer().serialize(reader));
    }

    @Override
    public void setDayClassCommands(DayClassCommands dayClassCommands) throws ElkrommException
    {
        sendCommand(ElkronCommand.DAY_CLASS_CMDS, ElkrommFactory.getFactory().getDayClassCommandsSerializer().serialize(dayClassCommands));
    }
}
