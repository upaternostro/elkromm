package org.paternostro.elkromm.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;

import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Credential.Enabling;
import org.paternostro.elkromm.dto.DayClassCommands;
import org.paternostro.elkromm.dto.EnableDisableUser;
import org.paternostro.elkromm.dto.ExcludeIncludeInput;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Login;
import org.paternostro.elkromm.dto.PartitionArming;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SingleCredential;
import org.paternostro.elkromm.dto.SingleKeyboard;
import org.paternostro.elkromm.dto.SingleSMS;
import org.paternostro.elkromm.dto.User;
import org.paternostro.elkromm.dto.UserEnablings;
import org.paternostro.elkromm.packet.ElkrommPacket;
import org.paternostro.mock.ipc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ClientConnection extends Thread {
    public static final Logger logger = LoggerFactory.getLogger(ClientConnection.class);

    protected ElkrommFacade.Status                          status = ElkrommFacade.Status.ST_DISCONNECTED;
    protected Endpoint                                      endpoint;
    protected Model                                         model;
    protected Map<ElkronCommand, SortedSet<ElkrommPacket>>  cmdPayloads;
    protected Config                                        config;

    public ClientConnection(Endpoint endpoint, Model model) {
        assert endpoint != null : "Socket cannot be null";
        assert endpoint.isConnected() : "Socket must be connected";

        this.endpoint = endpoint;
        this.status = ElkrommFacade.Status.ST_CONNECTED;
        this.model = model;
        this.cmdPayloads = new HashMap<>();
        this.config = Config.getInstance();
    }

    protected void enqueuePayload(ElkrommPacket packet, byte[] payload, Queue<ElkrommPacket> packetQueue) throws ElkrommException {
        byte[]  payloadPart;

        // cannot compute checksum here because not all payloads have an inner checksum

        if (logger.isDebugEnabled()) ElkrommUtils.dumpPayload(packet.getCommand(), payload);

        // split payload
        int totalPackets = ((payload == null ? 0 : payload.length) - 1) / ElkrommFacade.MAX_DATA_LENGTH; // numero di pacchetti totali (base 0), meno uno perché 140 byte entrano tutti nel primo pacchetto
        int from = 0;
        int to;
        int index = 0;

        if (totalPackets < 0) {
            // fix per payload vuoto/nullo
            totalPackets = 0;
        }

        while (index <= totalPackets) {
            to = Math.min(from + ElkrommFacade.MAX_DATA_LENGTH, payload == null ? 0 : payload.length);
            payloadPart = payload != null ? Arrays.copyOfRange(payload, from, to) : null;
            packetQueue.add(ElkrommPacket.packetFactoryAllocate(packet.getCommand(), packet.getPlantCode12(), packet.getPlantCode34(), totalPackets, index, payload == null ? 0 : payloadPart.length, payloadPart));
            from = to;
            index++;
        }
    }

    protected void processPayload(ElkrommPacket packet) {
        byte[]  totalPayload = packet.cachePayload(cmdPayloads);

        if (totalPayload != null) {
            switch (packet.getCommand()) {
                case SET_PARTITIONS_AND_AREAS:
                    this.model.setAreasAndPartitions(ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case SET_USERS:
                    Credential[]    users = ElkrommFactory.getFactory().getUsersSerializer().deserialize(totalPayload);

                    for (int i = 0; i < users.length; i++) {
                        this.model.getUsers()[i] = (User)users[i];
                    }

                    this.model.computeChecksum();
                    break;
                case USER_PROGRAMMING:
                    SingleCredential  user = ElkrommFactory.getFactory().getSingleUserSerializer().deserialize(totalPayload);

                    this.model.getUsers()[user.getIndex() - 1] = (User)user.getCredential();
                    this.model.computeChecksum();
                    break;
                case SET_KEYS:
                    Credential[]    keys = ElkrommFactory.getFactory().getKeysSerializer().deserialize(totalPayload);

                    for (int i = 0; i < keys.length; i++) {
                        this.model.getKeys()[i] = (Key)keys[i];
                    }

                    this.model.computeChecksum();
                    break;
                case KEY_PROGRAMMING:
                    SingleCredential  key = ElkrommFactory.getFactory().getSingleKeySerializer().deserialize(totalPayload);

                    this.model.getKeys()[key.getIndex() - 1] = (Key)key.getCredential();
                    this.model.computeChecksum();
                    break;
                case SET_READERS:
                    Reader[]    readers = ElkrommFactory.getFactory().getReadersSerializer().deserialize(totalPayload);

                    for (int i = 0; i < readers.length; i++) {
                        this.model.getReaders()[i] = (Reader)readers[i];
                    }

                    this.model.computeChecksum();
                    break;
                case READER_PROGRAMMING:
                    Reader  reader = ElkrommFactory.getFactory().getReaderSerializer().deserialize(totalPayload);

                    for (int i = 0; i < this.model.getReaders().length; i++) {
                        if (this.model.getReaders()[i].compareTo(reader) == 0) {
                            this.model.getReaders()[i] = reader;
                            this.model.computeChecksum();
                            break;
                        }
                    }
                    break;
                case SET_TIME_PROGRAMMER:
                    this.model.setTimeProgrammer(ElkrommFactory.getFactory().getTimeProgrammerSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case DAY_CLASS_CMDS:
                    DayClassCommands    dayClassCommands = ElkrommFactory.getFactory().getDayClassCommandsSerializer().deserialize(totalPayload);

                    switch (dayClassCommands.getDayClass()) {
                        case DCCDC_WORKING_DAY:
                            this.model.getTimeProgrammer().setWorkingDaysCommands(dayClassCommands.getCommands());
                            break;
                        case DCCDC_PRE_HOLIDAY:
                            this.model.getTimeProgrammer().setPreHolidayDaysCommands(dayClassCommands.getCommands());
                            break;
                        case DCCDC_HOLIDAY:
                            this.model.getTimeProgrammer().setHolidayDaysCommands(dayClassCommands.getCommands());
                            break;
                    }
                    
                    this.model.computeChecksum();
                    break;
                case SET_PARAMETERS_ENABLINGS:
                    this.model.setParametersEnablings(ElkrommFactory.getFactory().getParametersEnablingsSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case SET_PHONE_PARAMETERS:
                    this.model.setPhoneParameters(ElkrommFactory.getFactory().getPhoneParametersSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case SET_PSTN_GSM:
                    this.model.setPSTNGSM(ElkrommFactory.getFactory().getPSTNGSMSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case SET_PHONE_NUMBERS:
                    this.model.setPhoneNumbers(ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case SET_SMS:
                    this.model.setSMSs(ElkrommFactory.getFactory().getSMSsSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case SMS_PROGRAMMING:
                    SingleSMS   singleSMS = ElkrommFactory.getFactory().getSingleSMSSerializer().deserialize(totalPayload);

                    this.model.getSMSs().getSMSs()[singleSMS.getIndex().ordinal()] = singleSMS.getSMS();
                    this.model.computeChecksum();
                    break;
                case SET_C200B:
                    this.model.setC200bParameters(ElkrommFactory.getFactory().getC200bParametersSerializer().deserialize(totalPayload));
                    this.model.computeChecksum();
                    break;
                case KEYPAD_PROGRAMMING:
                    SingleKeyboard  singleKeyboard = ElkrommFactory.getFactory().getSingleKeyboardSerializer().deserialize(totalPayload);

                    this.model.getKeyboards()[singleKeyboard.getIndex() - 1] = singleKeyboard.getKeyboard();
                    this.model.computeChecksum();
                    break;
                case ARM_DISARM_PARTITION:
                    PartitionArming pa = ElkrommFactory.getFactory().getPartitionArmingSerializer().deserialize(totalPayload);
                    byte            value;

                    for (int i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
                        value = ElkrommFacade.Partition.values()[i+1].getValue();

                        if ((byte)(pa.getPartitions() & value) != 0x00) {
                            this.model.getSystemStatus().setPartitionArming(ElkrommFacade.Partition.valueOf((byte)(1 << i)), (byte)(pa.getArmStatus() & value) != 0x00);
                        }
                    }

                    this.model.computeChecksum();
                    break;
                case EXCLUDE_INCLUDE_INPUT:
                    ExcludeIncludeInput eii = ElkrommFactory.getFactory().getExcludeIncludeInputSerializer().deserialize(totalPayload);

                    logger.debug(String.format("%sabling input %d", !eii.isEnabled() ? "En" : "Dis", eii.getOrdinal()));
                    logger.debug(String.format("Value before 0x%02X", model.getInputStatus()[eii.getOrdinal()]));

                    if (!eii.isEnabled()) {
                        model.getInputStatus()[eii.getOrdinal() - 1] &= (ElkrommFacade.InputStatus.IS_EXCLUDED.getBitMask() ^ ElkrommFacade.InputStatus.IS_ALL.getBitMask());
                    } else {
                        model.getInputStatus()[eii.getOrdinal() - 1] |= ElkrommFacade.InputStatus.IS_EXCLUDED.getBitMask();
                    }

                    logger.debug(String.format("Value after 0x%02X", model.getInputStatus()[eii.getOrdinal()]));
                    
                    break;
                case ENABLE_DISABLE_USER:
                    EnableDisableUser   edu = ElkrommFactory.getFactory().getEnableDisableUserSerializer().deserialize(totalPayload);
                    User                userEn = model.getUsers()[edu.getOrdinal() - 1];

                    if (userEn.getEnabling() != Enabling.ALWAYS_ENABLED) {
                        userEn.setEnabling(edu.isEnabled() ? Enabling.ENABLED : Enabling.DISABLED);
                    }

                    this.model.computeChecksum();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void run() {
        // Handle client connection here
        InputStream             inputStream = null;
        OutputStream            outputStream = null;
        boolean                 doNotAnswer = false;
        ElkrommPacket           packet;
        byte[]                  payload = null;
        Queue<ElkrommPacket>    packetQueue = new LinkedList<>();
        final byte[]            EMPTY_PAYLOAD = {  };
        int                     plantCode = config.getPlantCode();

        logger.info("Handling client request...");

        try {
            inputStream = endpoint.getInputStream();
            outputStream = endpoint.getOutputStream();

            while (true) { // Read data until the client closes the connection
                packet = ElkrommPacket.deserialize(inputStream);

                if (packet == null) {
                    break;
                }

                doNotAnswer = false;

                logger.info("Status: " + status);
                logger.info("Received data: " + packet);

                if (packet.getPlantCode12() != ElkrommUtils.bcdByte(plantCode / 1000000)) {
                    logger.warn(String.format("In plant code12: %02d expected: %02d", packet.getPlantCode12(), ElkrommUtils.bcdByte(plantCode / 1000000)));
                    ElkrommUtils.sendNAK(outputStream);
                    continue;
                }

                if (packet.getPlantCode34() != ElkrommUtils.bcdByte((plantCode / 10000) % 100)) {
                    logger.warn(String.format("In plant code34: %02d expected: %02d", packet.getPlantCode34(), ElkrommUtils.bcdByte((plantCode / 10000) % 100)));
                    ElkrommUtils.sendNAK(outputStream);
                    continue;
                }

                switch (status) {
                    case ST_CONNECTED:
                        switch (packet.getCommand()) {
                            case HELLO:
                                ElkrommUtils.sendACK(outputStream);
                                doNotAnswer = true;
                                break;
                            case LOGIN:
                                Login   login = ElkrommFactory.getFactory().getLoginSerializer().deserialize(packet.getData());

                                if (login.getPlantCode() != plantCode) {
                                    logger.warn(String.format("In plant code in login: %08d expected: %08d", login.getPlantCode(), plantCode));
                                    ElkrommUtils.sendNAK(outputStream);
                                } else if (login.getTechnicalCode() != config.getTechnicalCode()) {
                                    logger.warn(String.format("In technical code in login: %06d expected: %06d", login.getTechnicalCode(), config.getTechnicalCode()));
                                    ElkrommUtils.sendNAK(outputStream);
                                } else {
                                    status = Status.ST_LOGGED_IN;
                                    ElkrommUtils.sendACK(outputStream);
                                }

                                doNotAnswer = true;
                                break;
                            default:
                                ElkrommUtils.sendNAK(outputStream);
                                doNotAnswer = true;
                                break;
                        }
                        break;
                    case ST_LOGGED_IN:
                        switch (packet.getCommand()) {
                            case HELLO:
                            case LOGIN:
                                ElkrommUtils.sendNAK(outputStream);
                                doNotAnswer = true;
                                break;
                            case LOGOUT:
                                status = Status.ST_CONNECTED;
                                break;
                            case SYSTEM_STATUS:
                                payload = ElkrommFactory.getFactory().getSystemStatusSerializer().serialize(model.getSystemStatus());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PERIPHERAL_UNITS_ADDRESSES:
                                payload = ElkrommFactory.getFactory().getPeripheralUnitsSerializer().serialize(model.getPeripheralUnits());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case CHECKSUM:
                                payload = ElkrommFactory.getFactory().getChecksumsSerializer().serialize(model.getChecksums());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PARTITIONS_AND_AREAS:
                                payload = ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().serialize(model.getAreasAndPartitions());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case USERS:
                                payload = ElkrommFactory.getFactory().getUsersSerializer().serialize(model.getUsers());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case KEYS:
                                payload = ElkrommFactory.getFactory().getKeysSerializer().serialize(model.getKeys());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PARAMETERS_ENABLINGS:
                                payload = ElkrommFactory.getFactory().getParametersEnablingsSerializer().serialize(model.getParametersEnablings());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case EXPANSIONS:
                                payload = ElkrommFactory.getFactory().getExpansionsSerializer().serialize(model.getExpansions());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case KEYPADS:
                                payload = ElkrommFactory.getFactory().getKeyboardsSerializer().serialize(model.getKeyboards());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case TIME_PROGRAMMER:
                                payload = ElkrommFactory.getFactory().getTimeProgrammerSerializer().serialize(model.getTimeProgrammer());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case READERS:
                                payload = ElkrommFactory.getFactory().getReadersSerializer().serialize(model.getReaders());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PHONE_NUMBERS:
                                payload = ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().serialize(model.getPhoneNumbers());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PHONE_PARAMETERS:
                                payload = ElkrommFactory.getFactory().getPhoneParametersSerializer().serialize(model.getPhoneParameters());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PSTN_GSM:
                                payload = ElkrommFactory.getFactory().getPSTNGSMSerializer().serialize(model.getPSTNGSM());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case SMS:
                                payload = ElkrommFactory.getFactory().getSMSsSerializer().serialize(model.getSMSs());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case C200B:
                                payload = ElkrommFactory.getFactory().getC200bParametersSerializer().serialize(model.getC200bParameters());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case INPUT_STATUS:
                                enqueuePayload(packet, model.getInputStatus(), packetQueue);
                                break;
                            case USER_ENABLINGS:
                                payload = ElkrommFactory.getFactory().getUserEnablingsSerializer().serialize(new UserEnablings(model.getUserEnablings()));
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case EVENT_LOG:
                            case KEY_STATUS:
                                enqueuePayload(packet, EMPTY_PAYLOAD, packetQueue); // FIXME
                                break;
                            case SET_READERS:
                            case READER_PROGRAMMING:
                            case SET_PARTITIONS_AND_AREAS:
                            case SET_USERS:
                            case SET_KEYS:
                            case SET_PARAMETERS_ENABLINGS:
                            case SET_PHONE_NUMBERS:
                            case SET_PHONE_PARAMETERS:
                            case SET_PSTN_GSM:
                            case SET_SMS:
                            case SMS_PROGRAMMING: // no checksum
                            case SET_C200B:
                            case SET_TIME_PROGRAMMER:
                            case DAY_CLASS_CMDS:
                            case USER_PROGRAMMING:
                            case KEY_PROGRAMMING:
                            case ARM_DISARM_PARTITION:
                            case EXCLUDE_INCLUDE_INPUT:
                            case ENABLE_DISABLE_USER:
                            case KEYPAD_PROGRAMMING:
                                // solo SYN
                                processPayload(packet);
                                break;
                            case SEND:
                                // Nothing to be done here
                                break;
                            default:
                                ElkrommUtils.dumpPayload(packet.getCommand(), packet.getData());
                                break;
                        }

                        break;
                    default:
                        // SYN mandato per default sotto
                        break;
                }

                if (!doNotAnswer) {
                    ElkrommUtils.sendSYN(outputStream);

                    if (!packetQueue.isEmpty()) {
                        packetQueue.remove().serialize(outputStream);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error occurred while getting input stream", e);
            return;
        } catch (ElkrommException e) {
            logger.error("Error occurred while getting input stream", e);
            return;
        }

        logger.info("Client request handled.");
    }
}
