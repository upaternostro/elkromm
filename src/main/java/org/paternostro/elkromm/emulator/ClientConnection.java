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

        // // compute checksum
        // if (payload != null && payload.length > 4) {
        //     ElkrommUtils.computeBlockChecksum(payload);
        //     // ElkrommUtils.setLong(payload, payload.length - 4, ElkrommUtils.computeBlockChecksum(payload));
        // }

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
                case ARM_DISARM_SECTOR:
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
                                // packetQueue.add(new SystemStatus(packet.getPlantCode12(), packet.getPlantCode34(), 0, 0, payload.length, payload));
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case PERIPHERAL_UNITS_ADDRESSES:
                                payload = ElkrommFactory.getFactory().getPeripheralUnitsSerializer().serialize(model.getPeripheralUnits());
                                // packetQueue.add(new PeripheralUnitsAddresses(packet.getPlantCode12(), packet.getPlantCode34(), 0, 0, payload.length, payload));
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case CHECKSUM:
                                // int         c = 0;
                                // Checksums   checksums = new Checksums(-(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2), -(c++ +2));

                                // payload = ElkrommFactory.getFactory().getChecksumsSerializer().serialize(checksums);
                                payload = ElkrommFactory.getFactory().getChecksumsSerializer().serialize(model.getChecksums());
                                // packetQueue.add(new Checksum(packet.getPlantCode12(), packet.getPlantCode34(), 0, 0, payload.length, payload));
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
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff  -> 14 byte con il numero di telefono codificato BCD (verificare LAN?)
// f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. -> f8 boh, ma sotto è f0. Nibble basso partizioni. Forse tutto, ma ne ho solo 4?
// 00 Invece è il tipo: PSTN = 0x00, GSM = 0x01, LAN = 0x02
// 0010: 00 -> Sendong mode: 0x00 Voice, 0x01 IDP, 0x02 ADF, 0x04 Modem, 0x06 SMS, 0x07 C200b
// ff ff ff ff ff ff ff  ff ff ff ff ff ff ff f0   .￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￰ 
// 0020: 00 00 ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ..￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f0 00 00 ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￰..￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 00 00 00 ff ff ff ff  ff ff ff ff ff ff ff ff   ￿...￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 00 00 00 ff ff ff  ff ff ff ff ff ff ff ff   ￿￿...￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 00 00 00 ff ff  ff ff ff ff ff ff ff ff   ￿￿￿...￿￿ ￿￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 00 00 00 ff  ff ff ff ff ff ff ff ff   ￿￿￿￿...￿ ￿￿￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 00 00 00  ff ff ff ff ff ff ff ff   ￿￿￿￿￿... ￿￿￿￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 00 00  00 ff ff ff ff ff ff ff   ￿￿￿￿￿￿.. .￿￿￿￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 00  00 00 ff ff ff ff ff ff   ￿￿￿￿￿￿￿. ..￿￿￿￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  00 00 00 ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ...￿￿￿￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 00 00 00 00 04 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 04 00 00  00 04 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 05 00 00  00 05 00 00 00 05 00 00   ........ ........ 
// 00f0: 00 05 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 04 00 00  00 04 00 00 00 04 00 00   ........ ........ 
// 0110: 00 04 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 04 00 00 00 04 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 04 00 00 00 04 00 00  00 04 00 00 00 00 00 00   ........ ........ 
// 0140: 00 04 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 04 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 04 00 00  00 04 00 00 00 04 00 00   ........ ........ 
// 0170: 00 04 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 57 d5                            ....￿￿Wￕ 
// Checksum ffff57d5 is 

// un flag per numero (a scalare)
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 04 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 00 00 00  08 00 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 00f0: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 04 00 00 00  00 20 00 00 00 40 00 00   ........ . ...@.. 
// 0110: 00 10 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 80 00 00 00 00 00 00  00 00 00 00 00 00 00 00   .ﾀ...... ........ 
// 0130: 00 00 00 00 02 00 00 00  02 00 00 00 01 00 00 00   ........ ........ 
// 0140: 02 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 02 00 00  00 04 00 00 00 08 00 00   ........ ........ 
// 0170: 04 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 70 e7                            ....￿￿p￧ 
// Checksum ffff70e7 is 

// primo numero con tutti i flag, gli altri a scalare
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 
// 04 01 00 00   ￿￿￿￿￿￿￿￿ ￿....... // 11 // tampering 1/3 0xCC
// 00d0: 00 00 00 00 
// 00 01 00 00  // low battery
// 08 01 00 00 // mains power
// 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 
// 00 01 00 00  // burlgar 1/4
// 00 01 00 00  // burlgar 2/4
// 00 01 00 00   ........ ........ // burlgar 3/4
// 00f0: 00 01 00 00 // burlgar 4/4
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 
// 04 01 00 00  // tampering 2/3
// 00 21 00 00 // panic
// 00 41 00 00   ........ .!...A.. // silent panic
// 0110: 00 11 00 00 // fire alarm
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0120: 00 81 00 00 // medical emerg.
// 00 01 00 00   // system fault 1/2
// 00 00 00 00 
// 00 00 00 00   .ﾁ...... ........ 
// 0130: 00 01 00 00 // system fault 2/2
// 02 01 00 00  // partitions/system on/off 1/3
// 02 01 00 00 // partitions/system on/off 2/3
// 01 01 00 00   ........ ........ // hold-up
// 0140: 02 01 00 00 // partitions/system on/off 3/3
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 
// 00 00 00 00  
// 00 01 00 00 // notices
// 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 
// 00 03 00 00 // tech. alarm type 1
// 00 05 00 00 // tech. alarm type 2
// 00 09 00 00   ........ ........ // tech. alarm type 3
// 0170: 04 01 00 00 // tampering 3/3
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 
// ff ff 70 d4                            ....￿￿pￔ 
// Checksum ffff70d4 is 

// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff f0 00   ￿￿￿￿￿￿￿￿ ￿￿￿￿￿￿￰. 
// 0010: 00 ff ff ff ff ff ff ff  ff ff ff ff ff ff ff f0   .￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￰ 
// 0020: 00 00 ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ..￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f0 00 00 ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￰..￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 00 00 00 ff ff ff ff  ff ff ff ff ff ff ff ff   ￿...￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 00 00 00 ff ff ff  ff ff ff ff ff ff ff ff   ￿￿...￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 00 00 00 ff ff  ff ff ff ff ff ff ff ff   ￿￿￿...￿￿ ￿￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 00 00 00 ff  ff ff ff ff ff ff ff ff   ￿￿￿￿...￿ ￿￿￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 00 00 00  ff ff ff ff ff ff ff ff   ￿￿￿￿￿... ￿￿￿￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 00 00  00 ff ff ff ff ff ff ff   ￿￿￿￿￿￿.. .￿￿￿￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 00  00 00 ff ff ff ff ff ff   ￿￿￿￿￿￿￿. ..￿￿￿￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  00 00 00 ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ...￿￿￿￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 00 00 00
//  00 01 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00
//  00 03 00 00  // low battery
// 00 01 00 00 
// 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 
// 00 01 00 00  
// 00 01 00 00 
// 00 01 00 00   ........ ........ 
// 00f0: 00 01 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 
// 00 01 00 00  
// 00 01 00 00 
// 00 01 00 00   ........ ........ 
// 0110: 00 01 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0120: 00 01 00 00 
// 00 05 00 00  // system fault 1/2
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0130: 00 05 00 00  // system fault 2/2
// 00 01 00 00  
// 00 01 00 00 
// 00 01 00 00   ........ ........ 
// 0140: 00 01 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 
// 00 00 00 00  
// 00 09 00 00 // notices
// 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 
// 00 01 00 00  
// 00 01 00 00 
// 00 01 00 00   ........ ........ 
// 0170: 00 01 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 
// ff ff 55 af                            ....￿￿Uﾯ 
// Checksum ffff55af is valid

// Primo numero con tutti i flag, altri disattivati
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 00 01 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 01 00 00  00 01 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 00f0: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 0110: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 01 00 00 00 01 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 01 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 0140: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 01 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 0170: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 71 ed                            ....￿￿q￭ 
// Checksum ffff71ed is 

// Tutto disattivato
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 00 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00f0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0110: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0140: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0170: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 72 04                            ....￿￿r. 
// Checksum ffff7204 is 

// Burglar alarm su telefono 1
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 00 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 00f0: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0110: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0140: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0170: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 72 00                            ....￿￿r. 
// Checksum ffff7200 is 

// Burglar alarm su telefono 1 e 2
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 00 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 03 00 00  00 03 00 00 00 03 00 00   ........ ........ 
// 00f0: 00 03 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0110: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0140: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0170: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 71 f8                            ....￿￿q￸ 
// Checksum ffff71f8 is 

// Technological alarm type 1 su telefono 1
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 00 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00f0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0110: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0140: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 01 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0170: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 72 03                            ....￿￿r. 
// Checksum ffff7203 is 

// Technological alarm type 1 su telefono 1 e 2
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 56 ff ff ff ff ff  ff ff ff ff ff ff f8 00   .4V￿￿￿￿￿ ￿￿￿￿￿￿￸. 
// 0010: 00 12 34 56 ff ff ff ff  ff ff ff ff ff ff ff f8   ..4V￿￿￿￿ ￿￿￿￿￿￿￿￸ 
// 0020: 00 00 12 34 56 ff ff ff  ff ff ff ff ff ff ff ff   ...4V￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: f8 00 00 12 34 56 ff ff  ff ff ff ff ff ff ff ff   ￸...4V￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 08 00 00 12 34 56 ff  ff ff ff ff ff ff ff ff   ￿....4V￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 08 00 00 12 34 56  ff ff ff ff ff ff ff ff   ￿￿....4V ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 08 00 00 12 34  56 ff ff ff ff ff ff ff   ￿￿￿....4 V￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 08 00 00 12  34 56 ff ff ff ff ff ff   ￿￿￿￿.... 4V￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 08 00 00  12 34 56 ff ff ff ff ff   ￿￿￿￿￿... .4V￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 08 00  00 12 34 56 ff ff ff ff   ￿￿￿￿￿￿.. ..4V￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 08  00 00 12 34 56 ff ff ff   ￿￿￿￿￿￿￿. ...4V￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  08 00 00 12 34 56 ff ff   ￿￿￿￿￿￿￿￿ ....4V￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 08 00 00 00 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 00f0: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0110: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0140: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0150: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 03 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0170: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 72 01                            ....￿￿r. 
// Checksum ffff7201 is 

// IDP con altri tick abilitati
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 12 34 5f ff ff ff ff ff  ff ff ff ff ff ff ff 00   .4_￿￿￿￿￿ ￿￿￿￿￿￿￿. 
// 0010: 01 ff ff ff ff ff ff ff  ff ff ff ff ff ff ff 00   .￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿. 
// 0020: 00 01 ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ..￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: 00 00 01 ff ff ff ff ff  ff ff ff ff ff ff ff ff   ...￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 00 00 01 ff ff ff ff  ff ff ff ff ff ff ff ff   ￿...￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 00 00 01 ff ff ff  ff ff ff ff ff ff ff ff   ￿￿...￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 00 00 00 ff ff  ff ff ff ff ff ff ff ff   ￿￿￿...￿￿ ￿￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 00 00 00 ff  ff ff ff ff ff ff ff ff   ￿￿￿￿...￿ ￿￿￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 00 00 00  ff ff ff ff ff ff ff ff   ￿￿￿￿￿... ￿￿￿￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 00 00  00 ff ff ff ff ff ff ff   ￿￿￿￿￿￿.. .￿￿￿￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 00  00 00 ff ff ff ff ff ff   ￿￿￿￿￿￿￿. ..￿￿￿￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  00 00 00 ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ...￿￿￿￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 00 00 00 
// 00 00 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 00f0: 00 00 00 00 
// 00 03 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0110: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0120: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0130: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0140: 00 00 00 00 
// 00 09 00 00  
// 00 09 00 00 
// 00 00 00 00   ........ ........ 
// 0150: 00 05 00 00 
// 00 11 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0170: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 
// 00 00 00 00  
// 00 00 00 00 
// 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 
// ff ff 59 d1                            ....￿￿Y￑ 
// Checksum ffff59d1 is valid

// LAN/IP/modem
// Dumping SET_PHONE_NUMBERS, payload size: 408
// 0000: 17 2b 23 b2 3b 31 ff ff  ff ff ff ff ff ff ff 02   .+#ﾲ;1￿￿ ￿￿￿￿￿￿￿. 
// 0010: 04 ff ff ff ff ff ff ff  ff ff ff ff ff ff ff 00   .￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿. 
// 0020: 00 00 ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ..￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0030: 00 00 00 ff ff ff ff ff  ff ff ff ff ff ff ff ff   ...￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0040: ff 00 00 00 ff ff ff ff  ff ff ff ff ff ff ff ff   ￿...￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0050: ff ff 00 00 00 ff ff ff  ff ff ff ff ff ff ff ff   ￿￿...￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0060: ff ff ff 00 00 00 ff ff  ff ff ff ff ff ff ff ff   ￿￿￿...￿￿ ￿￿￿￿￿￿￿￿ 
// 0070: ff ff ff ff 00 00 00 ff  ff ff ff ff ff ff ff ff   ￿￿￿￿...￿ ￿￿￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff 00 00 00  ff ff ff ff ff ff ff ff   ￿￿￿￿￿... ￿￿￿￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff 00 00  00 ff ff ff ff ff ff ff   ￿￿￿￿￿￿.. .￿￿￿￿￿￿￿ 
// 00a0: ff ff ff ff ff ff ff 00  00 00 ff ff ff ff ff ff   ￿￿￿￿￿￿￿. ..￿￿￿￿￿￿ 
// 00b0: ff ff ff ff ff ff ff ff  00 00 00 ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ...￿￿￿￿￿ 
// 00c0: ff ff ff ff ff ff ff ff  ff 00 00 00 00 01 00 00   ￿￿￿￿￿￿￿￿ ￿....... 
// 00d0: 00 00 00 00 00 01 00 00  00 01 00 00 00 00 00 00   ........ ........ 
// 00e0: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 00f0: 00 01 00 00 00 01 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0100: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 0110: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0120: 00 01 00 00 00 01 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0130: 00 01 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 0140: 00 01 00 00 00 01 00 00  00 01 00 00 00 00 00 00   ........ ........ 
// 0150: 00 01 00 00 00 01 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0160: 00 00 00 00 00 01 00 00  00 01 00 00 00 01 00 00   ........ ........ 
// 0170: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0180: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0190: 00 00 00 00 ff ff 5b ff                            ....￿￿[￿ 
// Checksum ffff5bff is valid

// In generale gli IP sono 001B002B003B004C00005 (attivo se tipo = LAN)
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
// 496e74727573696f6e65ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff416c6c61726d65207465636e69636f2031ffffffffffffffffffffffffffffffffffffffffffffff416c6c61726d65207465636e69636f2032ffffffffffffffffffffffffffffffffffffffffffffff416c6c61726d65207465636e69636f2033ffffffffffffffffffffffffffffffffffffffffffffff496e63656e64696fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff50617274697a696f6e65206174746976617461ffffffffffffffffffffffffffffffffffffffffff50617274697a696f6e65206469736174746976617461ffffffffffffffffffffffffffffffffffff4d616e6f6d697373696f6e65ffffffffffffffffffffffffffffffffffffffffffffffffffffffff4e6f7465fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffee644
// 40 caratteri tamponati con 0xff
// totale 9 SMS (payload 140+140+84 compreso checksum)
// in ordine Intrusione, Allarme tecnico 1, 2, 3, Incendio, Partizione attivata, partizione disattivata, Manomissione, Note
                                payload = ElkrommFactory.getFactory().getSMSsSerializer().serialize(model.getSMSs());
                                enqueuePayload(packet, payload, packetQueue);
                                break;
                            case C200B:
// 00fe0203fefe1516171819fefefe1d1e1f20fe252624274431373d3e3f4045464d4f5558fefe2b2c2d06fefefefe80fefefeffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7886
//
// 00 fe 02 03 fe fe 15 16  17 18 19 fe fe fe 1d 1e
// 1f 20 fe 25 26 24 27 44  31 37 3d 3e 3f 40 45 46 
// 4d 4f 55 58 fe fe 2b 2c  2d 06 fe fe fe fe 80 fe 
// fe fe ff ff ff ff ff ff  ff ff ff ff ff ff ff ff 
// ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff 
// ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff 
// ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff 
// ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff 
// ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff
// ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff
// ff ff ff ff ff ff 78 86
// payload 168 compreso checksum
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
// Dumping SET_READERS, payload size: 117
// 0000: 01 00 00 00 00 00 
// 00 00  01 09 00 01 2e 2e 2e 00   ........ ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00 ff ff 00 
// 00 00 00 09   ........ .￿￿..... 
// 0030: 00 01 2e 2e 2e 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0040: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 ff   ........ .......￿ 
// 0050: ff 00 
// 01 02 04 08 00 
// 44  4b 20 20 30 31 00 00 00   ￿......D K  01... 
// 0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00
//  00   ........ ........ 
// 0070: 00 ff ff f9 9b                                     .￿￿￹ﾛ
// Checksum fffff99b is 
                            case READER_PROGRAMMING:
// Dumping READER_PROGRAMMING, payload size: 113
// 0000: 01 00 00 00 00 00 00 00  01 09 00 01 2e 2e 2e 00   ........ ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00 ff ff 00 00 00 00 09   ........ .￿￿..... 
// 0030: 00 01 2e 2e 2e 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0040: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 ff   ........ .......￿ 
// 0050: ff 00 09 04 02 00 01 4d  79 52 65 61 64 65 72 00   ￿......M yReader. 
// 0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0070: 00                                                 .
// Checksum 00000000 is in                                
                            case SET_PARTITIONS_AND_AREAS:
                            case SET_USERS:
                            case SET_KEYS:
                            case SET_PARAMETERS_ENABLINGS:
// Dumping SET_PARAMETERS_ENABLINGS, payload size: 30
// 0000: 00 00 00 00 00 01 00 01  00 01 00 01 00 01 00 01   ........ ........ 
// 0010: 01 55 55 55 55 01 0b 00  00 80 ff ff fe 19         .UUUU... .ﾀ￿￿￾.
// Checksum is 

//           UUUU  ����

// bulgar: 30 s -> 5, 7, 9: 0x00 = 30 s, 0x01 = 60 s, 0x02 = 90 s, 0x03 = 180 s, 0x04 = 9 m
// emerg: 30 s -> 8
// pre alarm: 30 s -> 6
// alarm count: Two -> 13: 0x00 = No Count, 0x01 = Two, 0x02 = Four, 0x03 = Six, 0x04 = Eight
// power lack: 2 h -> 11: 0x00 = 1 h. 0x01 = 2 h, 0x02 = 4 h

// play fault: false -> 24, lsb 0x01
// play sectors: false -> 24, bit 1 0x02
// play system: false -> 24, bit 2 0x04
// play service: false -> 24, bit 3 0x08

// help msg: enable (no select) -> 25, 0x80, nibble basso indirizzo tastiera - 1

// lan: disable -> 23 0x00 (al posto di 0x01)

// time progr: enable -> 15, 0x01

// Notice: no notice -> 14, 0x00 = No notice, 0x05 = 5 minutes, 0x0a = 10 minutes, 0x0f = 15 minutes, 0x14 = 20 minutes

// auto DST: disable -> 16, 0x01
// OFF: october -> 21
// ON: march -> 22
// sunday: last -> 16, 0x02 (0 = first, 0x02 = last)

                            case SET_PHONE_NUMBERS:
                            case SET_PHONE_PARAMETERS:
// Dumping SET_PHONE_PARAMETERS, payload size: 20
// 0000: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0010: 00 00 00 00                                        ....
// Checksum 00000000 is 
// byte 5 call delay 0x01 enable
// byte 7 return call 0x01 = type A, 0x02 = type B, 0x00 DISABLE
// byte 8 Remote Surveillance: 0x01 ENABLE
// byte 9 Voice messages Sending mode 0x01
// byte 11 Cyclic test call 0x01 = 24 hour, 0x02 = system on, 0x00 = DISABLE
// byte 12 Cyclic test call phone number index
// byte 13 Cyclic test call hour
// byte 14 Cyclic test call minute
// byte 15 Cyclic test interval: 0x00 = 1 h, 0x01 = 4 h, 0x02 = 8 h, 0x03 = 12 h, 0x04 = 24 h, 0x05 = 48 h, 0x06 = 72 h, 0x07 = 96 h, 0x08 = 120 h, 0x09 = 144 h, 0x0a = 168 h
                            case SET_PSTN_GSM:
// Dumping SET_PSTN_GSM, payload size: 21
// 0000: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0010: 00 ff ff ff ff                                     .￿￿￿￿
// Checksum ffffffff is 
// byte 0 Enable NET 0x01 = PSTN
// byte 1 Italy 0x00 -> France 0x01 -> Germany 0x02 -> Czech Republic 0x03 -> Poland 0x04 -> Spain 0x05 -> Portugal 0x06 -> Greece 0x07 -> England 0x08
// byte 4 Local access digit (PABX enabled). PABX DISABLE 0xff
// byte 5 Tone control, 0x01 ENABLE
// byte 6 Answer control, 0x01 ENABLE
// byte 7 PSTN line test 0x01 = 24 h, 0x02 = system on, 0x00 = DISABLE
// byte 8 PSTN Answering machine rings 2, 4, 8. DISABLE = 0
// byte 9 Enable NET GSM 0x01
// byte 10 GSM answering machine (no rings) 0x01 ENABLE
// byte 11 Incoming SMS 0x01 ENABLE
// byte 12-14 GSM PIN BCD
// byte 15: expiration month
// byte 16: expiration year
                            case SET_SMS:
                            case SMS_PROGRAMMING: // no checksum
// Dumping SMS_PROGRAMMING, payload size: 41
// 0000: 01 41 67 61 69 6e 3f ff  ff ff ff ff ff ff ff ff   .Again?￿ ￿￿￿￿￿￿￿￿ 
// 0010: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0020: ff ff ff ff ff ff ff ff  ff                        ￿￿￿￿￿￿￿￿ ￿
// Checksum ffffffff is invalid
                            case SET_C200B:
// Dumping SET_C200B, payload size: 168
// 0000: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0030: 00 00 00 00 00 00 00 00  0c 0c 0c 0c 00 00 00 00   ........ ........ 
// 0040: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0050: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0060: 00 00 00 00 ff ff 00 00  00 00 00 00 00 00 00 00   ....￿￿.. ........ 
// 0070: 00 00 00 00 00 00 00 00  00 00 ff ff ff ff ff ff   ........ ..￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 00a0: ff ff ff ff ff ff d3 fc                            ￿￿￿￿￿￿ￓ￼ 
// Checksum ffffd3fc is valid

// event codes da 1 a 20, rispettivamente burlgar, pre-alarm, tech type 1, 2 e 3, fire, panic, silent oanic, medical aid, hold up, system on/off, partition on/off maintenance, input excl/incl, tampering, mains power, low battery, system fault, false code, cyclical test call
// Dumping SET_C200B, payload size: 168
// 0000: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0030: 00 00 0f 00 11 10 00 00  01 01 01 01 02 00 00 00   ........ ........ 
// 0040: 0f 07 08 06 00 00 00 09  12 00 0c 12 0b 0b 0a 0b   ........ ........ 
// 0050: 0e 0e 14 0d 13 00 00 00  03 04 05 0f 00 00 00 00   ........ ........ 
// 0060: 00 00 00 00 ff ff 00 00  00 00 00 00 00 00 00 00   ....￿￿.. ........ 
// 0070: 00 00 00 00 00 00 00 00  00 00 ff ff ff ff ff ff   ........ ..￿￿￿￿￿￿ 
// 0080: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 0090: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff   ￿￿￿￿￿￿￿￿ ￿￿￿￿￿￿￿￿ 
// 00a0: ff ff ff ff ff ff d3 03                            ￿￿￿￿￿￿ￓ. 
// Checksum ffffd303 is va

// burlgar 0x38, 39, 3a, 3b
// pre-alarm 0x3c
// tech type 1 0x58
// tech type 2 0x59
// tech type 3 0x5a
// fire 0x43
// panic 0x41
// silent panic 0x42
// medical aid 0x47
// hold up 0x4e
// system on/off 0x4c, 0x4d, 0x4f
// partition on/off 0x4a,
// maintenance 0x53
// input excl/incl 0x50 e 0x51
// tampering 0x32, 0x40, 0x5b
// mains power 0x35
// low battery 0x34
// system fault 0x48 e 0x4b
// false code 0x54
// cyclical test call 0x52

// da 0x64 per 64 (0x40) bytes vengono riportati gli input codes dei vari input in sequenza, usando 0xff se l'input non esiste

                            case SET_TIME_PROGRAMMER:
// Dumping SET_TIME_PROGRAMMER, payload size: 131
// 0000: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0030: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0040: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0050: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0070: 00 00 00 00 00 00 00 00  00 00 00 00 00 01 02 ff   ........ .......￿ 
// 0080: ff ff fd                                           ￿￿�
// Checksum fffffffd is 
//
// TPParameters, monday -> 120: 0x00 = Working day, 0x01 = Pre holiday, 0x02 = Holiday
//               ...
//               sunday -> 126

// Dumping SET_TIME_PROGRAMMER, payload size: 131
// 0000: 01 0d 40 01 02 00 00 00  00 00 00 00 00 00 00 00   ..@..... ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0030: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0040: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0050: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0070: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 ff   ........ .......￿ 
// 0080: ff ff af                                           ￿￿ﾯ
// Checksum ffffffaf is 
// 5 byte a comando
// cmd 1: h 1, m 2, enable, user 13

// Dumping WORKING_DAY_CMD, payload size: 41
// 0000: 00 01 0d 40 01 02 
//          02 02  40 02 01 00 00 00 00 00   ...@.... @....... 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00                        ........ .
// Checksum 00000000 is in

// Dumping WORKING_DAY_CMD, payload size: 41
// 0000: 00 01 0d 40 01 02
//          02 02  40 02 01 
//          01 1f 40 17 3b   ...@.... @....@.; 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00                        ........ .
// Checksum 00000000 is in

// Dumping WORKING_DAY_CMD, payload size: 41
// 0000: 00 01 0d 40 01 02 
//          02 02  40 02 01
//          01 1f 40 17 3b   ...@.... @....@.; 
// 0010:    01 0b 10 16 3a 00 00 00  00 00 00 00 00 00 00 00   ....:... ........ 
// 0020: 00 00 00 00 00 00 00 00  00                        ........ .
// Checksum 00000000 is in

// byte 0: command, 1=enable, 2=disable 0=no action
// byte 1: object (see next byte)
// byte 2: object type 0x40 = user, 0x10 = sectors
// byte 3: hour
// byte 4: minute

// Dumping WORKING_DAY_CMD, payload size: 41
// 0000: 01 02 ff 10 04 07 00 00  00 00 00 00 00 00 00 00   ..￿..... ........ 
// 0010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   ........ ........ 
// 0020: 00 00 00 00 00 00 00 00  00                        ........ .
// Checksum 00000000 is in
                            case DAY_CLASS_CMDS:
                            case USER_PROGRAMMING:
                            case KEY_PROGRAMMING:
// Dumping KEY_PROGRAMMING, payload size: 27
// 0000: 01 08 ff 61 6e 74 61 6e  69 00 00 00 00 00 00 00   ..￿antan i....... 
// 0010: 00 00 00 00 00 00 00 00  00 00 00                  ........ ...
// Checksum 00000000 is invalid
                            case ARM_DISARM_SECTOR:
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
        // } finally {
        //     if (inputStream != null) {
        //         try {
        //             inputStream.close();
        //         } catch (IOException e) {
        //             logger.error("Error occurred while closing input stream", e);
        //         }
        //     }
            
        //     try {
        //         endpoint.close(); // Close the client connection
        //         status = ElkrommFacade.Status.ST_DISCONNECTED;
        //     } catch (IOException e) {
        //         logger.error("Error occurred while closing socket", e);
        //     }
        }

        logger.info("Client request handled.");
    }
}
