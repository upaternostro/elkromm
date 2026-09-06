package org.paternostro.elkromm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade.InputStatus;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.DayClassCommands;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.PSTNGSM.Enabling;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumber;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.PhoneParameters.ReturnCall;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMS;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SingleCredential;
import org.paternostro.elkromm.dto.SMSs.SMSIndex;
import org.paternostro.elkromm.dto.SingleKeyboard;
import org.paternostro.elkromm.dto.SingleSMS;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
import org.paternostro.elkromm.dto.User;
import org.paternostro.elkromm.emulator.ClientConnection;
import org.paternostro.elkromm.emulator.Model;
import org.paternostro.mock.ipc.Channel;
import org.paternostro.mock.ipc.EndpointFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ElkrommFacadeFunctionalTest {
    private static ElkrommFacade    facade;
    private static ClientConnection server;

    @BeforeClass
    public static void initTests() throws UnknownHostException, IOException
    {
        Channel c2s = new Channel();
        Channel s2c = new Channel();
        
        server = new ClientConnection(EndpointFactory.getFactory().getPipeEndpoint(s2c, c2s), new Model());
        server.start();

        ElkrommFactory factory = ElkrommFactory.getFactory();

        assertNotNull(factory);

        facade = factory.getElkrommFacade(EndpointFactory.getFactory().getPipeEndpoint(c2s, s2c), 12345678);

        assertNotNull(facade);

        try {
            facade.setDelay(0);
        } catch (ElkrommException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
        
        assertEquals(facade.getStatus(), Status.ST_DISCONNECTED);

        try {
            facade.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
        
        try {
            facade.login(12345678, 987654);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    @Test
    public void testPing() throws UnknownHostException {
        try {
            facade.ping();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testAreasAndPartitions() throws UnknownHostException {
        try {
            AreasAndPartitions areasPartitions = facade.getAreasAndPartitions();
            
            assertNotNull(areasPartitions);
            assertTrue(areasPartitions.getAreaNum() >= 0 && areasPartitions.getAreaNum() <= ElkrommFacade.MAX_AREAS);
            assertTrue(areasPartitions.getPartitionNum() > 0 && areasPartitions.getPartitionNum() <= ElkrommFacade.MAX_PARTITIONS);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testSystemStatus() throws UnknownHostException {
        try {
            SystemStatus    systemStatus = facade.getSystemStatus();

            assertNotNull(systemStatus);
            assertNotNull(systemStatus.getActivePartitions());
            assertTrue(systemStatus.getActivePartitions().length == 8);
        
            for (int i = 0; i < 8; i++) {
                assertTrue(!systemStatus.getActivePartitions()[i]);
            };
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testArmDisarmSector() throws UnknownHostException {
        try {
            facade.armDisarmSector(ElkrommFacade.Partition.P_ONE, true);

            SystemStatus    systemStatus = facade.getSystemStatus();

            assertNotNull(systemStatus);
            assertNotNull(systemStatus.getActivePartitions());
            assertTrue(systemStatus.getActivePartitions().length == 8);
        
            for (int i = 0; i < 8; i++) {
                assertTrue(i == 0 ? systemStatus.getActivePartitions()[i] : !systemStatus.getActivePartitions()[i]);
            };

            facade.armDisarmSector(ElkrommFacade.Partition.P_ONE, false);
            systemStatus = facade.getSystemStatus();

            assertNotNull(systemStatus);
            assertNotNull(systemStatus.getActivePartitions());
            assertTrue(systemStatus.getActivePartitions().length == 8);
        
            for (int i = 0; i < 8; i++) {
                assertTrue(!systemStatus.getActivePartitions()[i]);
            };
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testRawInputStatus() throws ElkrommException {
        byte[] inputStatus = facade.getRawInputStatus();

        assertNotNull(inputStatus);
        assertTrue(inputStatus.length > 0);
    }

    @Test
    public void testInputStatus() throws ElkrommException {
        Map<InputStatus, List<Integer>> inputStatus = facade.getInputStatus();
        List<Integer>                   inputs;

        for (InputStatus pivot : inputStatus.keySet()) {
            inputs = inputStatus.get(pivot);

            System.err.print(pivot);
            System.err.print(": ");

            for (Integer address : inputs) {
                System.err.print(String.format("0x%02X, ", address));
            }

            System.err.println();
        }
    }

    @Test
    public void testExcludeIncludeInput() throws ElkrommException {
        facade.excludeIncludeInput((byte)1, true);

        Map<InputStatus, List<Integer>> inputStatus = facade.getInputStatus();

        assertTrue(inputStatus.get(InputStatus.IS_EXCLUDED).contains(1));
        facade.excludeIncludeInput((byte)1, false);
        inputStatus = facade.getInputStatus();
        assertTrue(!inputStatus.get(InputStatus.IS_EXCLUDED).contains(1));
    }

    @Test
    public void testPeripheralUnitsAddresses() throws ElkrommException {
        PeripheralUnits pu = facade.getPeripheralUnitsAddresses();

        assertNotNull(pu);
        assertTrue(pu.getExpansionNum() > 0);
        assertTrue(pu.getKeypadNum() >= 0);
        assertTrue(pu.getReaderNum() >= 0);
    }

    @Test
    public void testChecksums() throws ElkrommException {
        Checksums checksums = facade.getChecksums();

        assertNotNull(checksums);

        AreasAndPartitions          ap = facade.getAreasAndPartitions();

        assertNotNull(ap);

        byte[]                      data = ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().serialize(ap);

        assertTrue(checksums.getAreasAndPartitions() == ElkrommUtils.getLong(data, data.length - 4));

        C200bParameters             c200b = facade.getC200bParameters();
        
        assertNotNull(c200b);
        data = ElkrommFactory.getFactory().getC200bParametersSerializer().serialize(c200b);
        assertTrue(checksums.getEvents() == ElkrommUtils.getLong(data, data.length - 4));

        Keyboard[]                  keyboards = facade.getKeyboards();
        
        assertNotNull(keyboards);
        data = ElkrommFactory.getFactory().getKeyboardsSerializer().serialize(keyboards);
        assertTrue(checksums.getKeypads() == ElkrommUtils.getLong(data, data.length - 4));

        Credential[]                keys = facade.getKeys();
        
        assertNotNull(keys);
        data = ElkrommFactory.getFactory().getKeysSerializer().serialize(keys);
        assertTrue(checksums.getKeys() == ElkrommUtils.getLong(data, data.length - 4));

        Expansion[]                 expansions = facade.getExpansions();
        
        assertNotNull(expansions);
        data = ElkrommFactory.getFactory().getExpansionsSerializer().serialize(expansions);
        assertTrue(checksums.getNodes() == ElkrommUtils.getLong(data, data.length - 4));

        PSTNGSM                     pSTNGSM = facade.getPSTNGSM();
        
        assertNotNull(pSTNGSM);
        data = ElkrommFactory.getFactory().getPSTNGSMSerializer().serialize(pSTNGSM);
        assertTrue(checksums.getPstnGsm() == ElkrommUtils.getLong(data, data.length - 4));

        Reader[]                    readers = facade.getReaders();
        
        assertNotNull(readers);
        data = ElkrommFactory.getFactory().getReadersSerializer().serialize(readers);
        assertTrue(checksums.getReaders() == ElkrommUtils.getLong(data, data.length - 4));

        SMSs                        sMSs = facade.getSMSs();
        
        assertNotNull(sMSs);
        data = ElkrommFactory.getFactory().getSMSsSerializer().serialize(sMSs);
        assertTrue(checksums.getSms() == ElkrommUtils.getLong(data, data.length - 4));

        ParametersEnablings         pe = facade.getParametersEnablings();
        
        assertNotNull(pe);
        data = ElkrommFactory.getFactory().getParametersEnablingsSerializer().serialize(pe);
        assertTrue(checksums.getSystem() == ElkrommUtils.getLong(data, data.length - 4));

        PhoneNumbersSendingCodes    pnsc = facade.getPhoneNumbersSendingCodes();
        
        assertNotNull(pnsc);
        data = ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().serialize(pnsc);
        assertTrue(checksums.getTelephoneNumbers() == ElkrommUtils.getLong(data, data.length - 4));

        PhoneParameters             pp = facade.getPhoneParameters();
        
        assertNotNull(pp);
        data = ElkrommFactory.getFactory().getPhoneParametersSerializer().serialize(pp);
        assertTrue(checksums.getTelephoneParameters() == ElkrommUtils.getLong(data, data.length - 4));

        TimeProgrammer              tp = facade.getTimeProgrammer();
        
        assertNotNull(tp);
        data = ElkrommFactory.getFactory().getTimeProgrammerSerializer().serialize(tp);
        assertTrue(checksums.getTimeProgrammer() == ElkrommUtils.getLong(data, data.length - 4));

        Credential[]                users = facade.getUsers();
        
        assertNotNull(users);
        data = ElkrommFactory.getFactory().getUsersSerializer().serialize(users);
        assertTrue(checksums.getUsers() == ElkrommUtils.getLong(data, data.length - 4));
    }

    @Test
    public void testUsers() throws ElkrommException {
        Credential[]    users = facade.getUsers();

        assertNotNull(users);
        assertTrue(users.length == ElkrommFacade.MAX_CREDENTIALS);

        for (int i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            assertNotNull(users[i]);
            assertTrue(users[i] instanceof User);
        }

        assertEquals(users[1].getEnabling(), Credential.Enabling.ALWAYS_ENABLED);
    }

    @Test
    public void testKeys() throws ElkrommException {
        Credential[]    keys = facade.getKeys();

        assertNotNull(keys);
        assertTrue(keys.length == ElkrommFacade.MAX_CREDENTIALS);

        for (int i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            assertNotNull(keys[i]);
            assertTrue(keys[i] instanceof Key);
        }
    }

    @Test
    public void testExpansions() throws ElkrommException {
        Expansion[]    expansions = facade.getExpansions();

        assertNotNull(expansions);
        assertTrue(expansions.length <= ElkrommFacade.MAX_EXPANSIONS);

        for (int i = 0; i < expansions.length; i++) {
            assertNotNull(expansions[i]);
            assertNotNull(expansions[i].getName());
            assertNotNull(expansions[i].getVersion());
            assertTrue(expansions[i].getInputNum() > 0);
            assertTrue(expansions[i].getOutputNum() > 0);
        }
    }

    @Test
    public void testUserEnablings() throws ElkrommException {
        boolean[]   userEnablings = facade.getUserEnablings();

        assertNotNull(userEnablings);
        assertTrue(userEnablings.length == ElkrommFacade.MAX_CREDENTIALS);
        assertTrue(userEnablings[1]); // MASTER always enabled
    }

    @Test
    public void testEnableDisableUser() throws ElkrommException {
        boolean[]   userEnablings = facade.getUserEnablings();

        assertNotNull(userEnablings);
        assertTrue(userEnablings.length == ElkrommFacade.MAX_CREDENTIALS);
        assertTrue(!userEnablings[0]);

        facade.enableDisableUser((byte)1, true);

        userEnablings = facade.getUserEnablings();
        assertNotNull(userEnablings);
        assertTrue(userEnablings.length == ElkrommFacade.MAX_CREDENTIALS);
        assertTrue(userEnablings[0]);

        facade.enableDisableUser((byte)1, false);

        userEnablings = facade.getUserEnablings();
        assertNotNull(userEnablings);
        assertTrue(userEnablings.length == ElkrommFacade.MAX_CREDENTIALS);
        assertTrue(!userEnablings[0]);
        // MASTER cannot be disabled
        assertTrue(userEnablings[1]);

        facade.enableDisableUser((byte)2, false);

        userEnablings = facade.getUserEnablings();
        assertNotNull(userEnablings);
        assertTrue(userEnablings.length == ElkrommFacade.MAX_CREDENTIALS);
        assertTrue(userEnablings[1]);
    }

    @Test
    public void testKeyboards() throws ElkrommException {
        Keyboard[]  keyboards = facade.getKeyboards();

        assertNotNull(keyboards);
        assertTrue(keyboards.length > 0);
        assertNotNull(keyboards[0].getName());
    }

    @Test
    public void testParametersEnablings() throws ElkrommException {
        ParametersEnablings pe = facade.getParametersEnablings();

        assertNotNull(pe);
        assertNotNull(pe.getLan());
    }

    @Test
    public void testPhoneNumbersSendingCodes() throws ElkrommException {
        PhoneNumbersSendingCodes pnsc = facade.getPhoneNumbersSendingCodes();

        assertNotNull(pnsc);
        assertNotNull(pnsc.getPhoneNumbers());
    }

    @Test
    public void testPhoneParameters() throws ElkrommException {
        PhoneParameters pp = facade.getPhoneParameters();

        assertNotNull(pp);
        assertNotNull(pp.getCallDelay());
    }

    @Test
    public void testPSTNGSM() throws ElkrommException {
        PSTNGSM pSTNGSM = facade.getPSTNGSM();

        assertNotNull(pSTNGSM);
        assertNotNull(pSTNGSM.getCountry());
        assertNotNull(pSTNGSM.getEnableGSM());
        assertNotNull(pSTNGSM.getEnablePSTN());
    }

    @Test
    public void testReaders() throws ElkrommException {
        Reader[]    reader = facade.getReaders();

        assertNotNull(reader);
    }

    @Test
    public void testSMSs() throws ElkrommException {
        SMSs    sMSs = facade.getSMSs();

        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertNotNull(sMSs.getSMSs()[0]);
        assertNotNull(sMSs.getSMSs()[0].getText());
    }

    @Test
    public void testC200bParameters() throws ElkrommException {
        C200bParameters c200bParameters = facade.getC200bParameters();

        assertNotNull(c200bParameters);
        assertNotNull(c200bParameters.getEventCodes());
        assertNotNull(c200bParameters.getInputCodes());
    }

    @Test
    public void testTimeProgrammer() throws ElkrommException {
        TimeProgrammer   timeProgrammer = facade.getTimeProgrammer();

        assertNotNull(timeProgrammer);
        assertNotNull(timeProgrammer.getDayClasses());
        assertNotNull(timeProgrammer.getWorkingDaysCommands());
        assertNotNull(timeProgrammer.getWorkingDaysCommands()[0]);
        assertNotNull(timeProgrammer.getPreHolidayDaysCommands());
        assertNotNull(timeProgrammer.getPreHolidayDaysCommands()[0]);
        assertNotNull(timeProgrammer.getHolidayDaysCommands());
        assertNotNull(timeProgrammer.getHolidayDaysCommands()[0]);
    }

    @Test
    public void testArmDisarmSectors() throws ElkrommException {
        SystemStatus    status = facade.getSystemStatus();

        assertNotNull(status);
        assertNotNull(status.getActivePartitions());
        assertTrue(!status.getActivePartitions()[0]);

        facade.armDisarmSectors(ElkrommFacade.Partition.P_ONE.getValue(), ElkrommFacade.Partition.P_ONE.getValue());

        status = facade.getSystemStatus();

        assertNotNull(status);
        assertNotNull(status.getActivePartitions());
        assertTrue(status.getActivePartitions()[0]);

        facade.armDisarmSectors(ElkrommFacade.Partition.P_ONE.getValue(), (byte)0);

        status = facade.getSystemStatus();

        assertNotNull(status);
        assertNotNull(status.getActivePartitions());
        assertTrue(!status.getActivePartitions()[0]);
    }

    @Test
    public void testSetParametersEnablings() throws ElkrommException {
        ParametersEnablings pe = facade.getParametersEnablings();

        assertNotNull(pe);

        byte                oldHelp = pe.getHelp();
        byte                newHelp = (byte)(ParametersEnablings.Help.PEH_ENABLE.getValue() | 0x05);

        pe.setHelp(newHelp);
        facade.setParametersEnablings(pe);
        pe = facade.getParametersEnablings();
        assertNotNull(pe);
        assertTrue(pe.getHelp() == newHelp);
        pe.setHelp(oldHelp);
        facade.setParametersEnablings(pe);
        pe = facade.getParametersEnablings();
        assertNotNull(pe);
        assertTrue(pe.getHelp() == oldHelp);
    }

    @Test
    public void testSetAreasAndPartitions() throws ElkrommException {
        AreasAndPartitions  ap = facade.getAreasAndPartitions();

        assertNotNull(ap);
        assertNotNull(ap.getPartition(1));

        String              oldName = ap.getPartition(1).getName();

        ap.getPartition(1).setName("a name");
        facade.setAreasAndPartitions(ap);
        ap = facade.getAreasAndPartitions();
        assertNotNull(ap);
        assertNotNull(ap.getPartition(1));
        assertTrue("a name".equals(ap.getPartition(1).getName()));
        ap.getPartition(1).setName(oldName);
        facade.setAreasAndPartitions(ap);
        ap = facade.getAreasAndPartitions();
        assertNotNull(ap);
        assertNotNull(ap.getPartition(1));
        if (oldName != null) assertTrue(oldName.equals(ap.getPartition(1).getName()));
    }

    @Test
    public void testSetPhoneParameters() throws ElkrommException {
        PhoneParameters pp = facade.getPhoneParameters();

        assertNotNull(pp);
        assertNotNull(pp.getReturnCall());

        ReturnCall      oldReturnCall = pp.getReturnCall();

        pp.setReturnCall(PhoneParameters.ReturnCall.PPRC_TYPE_B);
        facade.setPhoneParameters(pp);
        pp = facade.getPhoneParameters();
        assertNotNull(pp);
        assertNotNull(pp.getReturnCall());
        assertTrue(pp.getReturnCall() == PhoneParameters.ReturnCall.PPRC_TYPE_B);
        pp.setReturnCall(oldReturnCall);
        facade.setPhoneParameters(pp);
        pp = facade.getPhoneParameters();
        assertNotNull(pp);
        assertNotNull(pp.getReturnCall());
        assertTrue(pp.getReturnCall() == oldReturnCall);
    }

    @Test
    public void testSetPhoneNumbersSendingCodes() throws ElkrommException {
        PhoneNumbersSendingCodes    pnsc = facade.getPhoneNumbersSendingCodes();

        assertNotNull(pnsc);
        assertNotNull(pnsc.getPhoneNumbers());
        assertTrue(pnsc.getPhoneNumbers().length > 0);

        PhoneNumber[]               phoneNumbers = pnsc.getPhoneNumbers();
        PhoneNumber                 oldPhoneNumber = phoneNumbers[0];
        PhoneNumber.Event[]         events = { PhoneNumber.Event.PNSCE_BURGLAR_ALARM };

        phoneNumbers[0] = new PhoneNumber("001.002.003.004:00005", ElkrommUtils.unpackPartitions((byte)0x01), PhoneNumber.Type.PNT_LAN, PhoneNumber.SendingMode.PNSM_MODEM, events);
        pnsc.setPhoneNumbers(phoneNumbers);
        facade.setPhoneNumbersSendingCodes(pnsc);
        pnsc = facade.getPhoneNumbersSendingCodes();
        assertNotNull(pnsc);
        assertNotNull(pnsc.getPhoneNumbers());
        assertTrue(pnsc.getPhoneNumbers().length > 0);
        assertNotNull(pnsc.getPhoneNumbers()[0]);
        assertTrue("001.002.003.004:00005".equals(pnsc.getPhoneNumbers()[0].getPhoneNumber()));
        assertTrue(ElkrommUtils.packPartitions(pnsc.getPhoneNumbers()[0].getAssociatedPartitions()) == 0x01);
        assertTrue(pnsc.getPhoneNumbers()[0].getType() == PhoneNumber.Type.PNT_LAN);
        assertTrue(pnsc.getPhoneNumbers()[0].getSendingMode() == PhoneNumber.SendingMode.PNSM_MODEM);

        Iterator<PhoneNumber.Event> it = pnsc.getPhoneNumbers()[0].getAssignedEventsIterator();

        assertNotNull(it);
        assertTrue(it.hasNext());
        assertTrue(it.next() == PhoneNumber.Event.PNSCE_BURGLAR_ALARM);
        assertTrue(!it.hasNext());
        phoneNumbers[0] = oldPhoneNumber;
        pnsc.setPhoneNumbers(phoneNumbers);
        facade.setPhoneNumbersSendingCodes(pnsc);
        pnsc = facade.getPhoneNumbersSendingCodes();
        assertNotNull(pnsc);
        assertNotNull(pnsc.getPhoneNumbers());
        assertTrue(pnsc.getPhoneNumbers().length > 0);
    }

    @Test
    public void testSetC200bParameters() throws ElkrommException {
        C200bParameters c200bParameters = facade.getC200bParameters();

        assertNotNull(c200bParameters);
        assertNotNull(c200bParameters.getInputCodes());

        byte[]          inputCodes = c200bParameters.getInputCodes();

        c200bParameters.setInputCode(1, (byte)0x42);
        facade.setC200bParameters(c200bParameters);
        c200bParameters = facade.getC200bParameters();
        assertNotNull(c200bParameters);
        assertNotNull(c200bParameters.getInputCodes());
        assertTrue(c200bParameters.getInputCodes()[1] == 0x42);
        c200bParameters.setInputCodes(inputCodes);
        facade.setC200bParameters(c200bParameters);
        c200bParameters = facade.getC200bParameters();
        assertNotNull(c200bParameters);
        assertNotNull(c200bParameters.getInputCodes());
    }

    @Test
    public void testSetSMSs() throws ElkrommException {
        SMSs    sMSs = facade.getSMSs();

        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertTrue(sMSs.getSMSs().length > 0);

        SMS[]   sMS = sMSs.getSMSs();
        SMS     oldSMS = sMS[0];

        sMS[0] = new SMS("a text");
        sMSs.setSMSs(sMS);
        facade.setSMSs(sMSs);
        sMSs = facade.getSMSs();
        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertTrue(sMSs.getSMSs().length > 0);
        assertTrue("a text".equals(sMSs.getSMSs()[0].getText()));
        sMS[0] = oldSMS;
        sMSs.setSMSs(sMS);
        facade.setSMSs(sMSs);
        sMSs = facade.getSMSs();
        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertTrue(sMSs.getSMSs().length > 0);
    }

    @Test
    public void testSetPSTNGSM() throws ElkrommException {
        PSTNGSM     pSTNGSM = facade.getPSTNGSM();

        assertNotNull(pSTNGSM);

        Enabling    oldEnableGSM = pSTNGSM.getEnableGSM();
        int         oldPIN = pSTNGSM.getGSMPin();

        pSTNGSM.setEnableGSM(PSTNGSM.Enabling.PGE_ENABLED);
        pSTNGSM.setGSMPin(12345);
        facade.setPSTNGSM(pSTNGSM);
        pSTNGSM = facade.getPSTNGSM();
        assertNotNull(pSTNGSM);
        assertTrue(pSTNGSM.getEnableGSM() == PSTNGSM.Enabling.PGE_ENABLED);
        assertEquals(12345, pSTNGSM.getGSMPin());
        pSTNGSM.setEnableGSM(oldEnableGSM);
        pSTNGSM.setGSMPin(oldPIN);
        facade.setPSTNGSM(pSTNGSM);
        pSTNGSM = facade.getPSTNGSM();
        assertNotNull(pSTNGSM);
    }

    @Test
    public void testSetUsers() throws ElkrommException {
        Credential[]    users = facade.getUsers();

        assertNotNull(users);
        assertTrue(users.length > 0);

        Credential      oldUser = users[2];

        users[2] = new User(3, "Test user", Credential.Enabling.ENABLED, ElkrommUtils.unpackPartitions((byte)0x01));
        facade.setUsers(users);
        users = facade.getUsers();
        assertNotNull(users);
        assertTrue(users.length > 0);
        assertEquals(3, users[2].getOrdinal());
        assertEquals("Test user", users[2].getName());
        assertEquals(Credential.Enabling.ENABLED, users[2].getEnabling());
        assertEquals(0x01, ElkrommUtils.packPartitions(users[2].getAssociatedPartitions()));
        users[2] = oldUser;
        facade.setUsers(users);
        users = facade.getUsers();
        assertNotNull(users);
        assertTrue(users.length > 0);
    }

    @Test
    public void testSetKeys() throws ElkrommException {
        Credential[]    keys = facade.getKeys();

        assertNotNull(keys);
        assertTrue(keys.length > 0);

        Credential      oldKey = keys[2];

        keys[2] = new Key(3, "Test user", Credential.Enabling.ENABLED, Key.Specialization.KS_CHANGE_PARTITION_STATUS, ElkrommUtils.unpackPartitions((byte)0x01));
        facade.setKeys(keys);
        keys = facade.getKeys();
        assertNotNull(keys);
        assertTrue(keys.length > 0);
        assertEquals(3, keys[2].getOrdinal());
        assertEquals("Test user", keys[2].getName());
        assertEquals(Credential.Enabling.ENABLED, keys[2].getEnabling());
        assertEquals(Key.Specialization.KS_CHANGE_PARTITION_STATUS, ((Key)keys[2]).getSpecialization());
        assertEquals(0x01, ElkrommUtils.packPartitions(keys[2].getAssociatedPartitions()));
        keys[2] = oldKey;
        facade.setKeys(keys);
        keys = facade.getKeys();
        assertNotNull(keys);
        assertTrue(keys.length > 0);
    }

    @Test
    public void testSetSMS() throws ElkrommException {
        SMSs        sMSs = facade.getSMSs();

        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertTrue(sMSs.getSMSs().length > 0);

        SMS[]       sMS = sMSs.getSMSs();
        SMS         oldSMS = sMS[0];
        SingleSMS   singleSMS = new SingleSMS(SMSIndex.valueOf(0), new SMS("a text"));

        facade.setSMS(singleSMS);
        sMSs = facade.getSMSs();
        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertTrue(sMSs.getSMSs().length > 0);
        assertTrue("a text".equals(sMSs.getSMSs()[0].getText()));
        singleSMS.setSMS(oldSMS);
        facade.setSMS(singleSMS);
        sMSs = facade.getSMSs();
        assertNotNull(sMSs);
        assertNotNull(sMSs.getSMSs());
        assertTrue(sMSs.getSMSs().length > 0);
    }

    @Test
    public void testSetKeyboard() throws ElkrommException {
        Keyboard[]      keyboards = facade.getKeyboards();

        assertNotNull(keyboards);
        assertTrue(keyboards.length > 0);

        Keyboard        oldKeyboard = keyboards[0];
        boolean[]       associatedPartitions = ElkrommUtils.unpackPartitions((byte)0x01);
        SingleKeyboard  singleKeyboard = new SingleKeyboard((byte)1, new Keyboard(1, "2.71", 
                                        new Input(1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input 1", Input.Delay.ID_30_SECS),
                                        new Input(2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input 2", Input.Delay.ID_20_SECS),
                                    Keyboard.Enablings.KE_ENTRY.getValue(), associatedPartitions, Keyboard.AudioFeatures.KA_NONE.getValue(), "Keyboard 1"));

        facade.setKeyboard(singleKeyboard);
        keyboards = facade.getKeyboards();
        assertNotNull(keyboards);
        assertTrue(keyboards.length > 0);
        assertEquals(1, keyboards[0].getAddress());
        assertEquals("2.71", keyboards[0].getVersion());
        ElkrommTestUtils.testInput(singleKeyboard.getKeyboard().getFirstInput(), keyboards[0].getFirstInput());
        ElkrommTestUtils.testInput(singleKeyboard.getKeyboard().getSecondInput(), keyboards[0].getSecondInput());
        assertEquals(Keyboard.Enablings.KE_ENTRY.getValue(), keyboards[0].getEnablings());
        assertEquals(0x01, ElkrommUtils.packPartitions(keyboards[0].getAssociatedPartitions()));
        assertEquals(Keyboard.AudioFeatures.KA_NONE.getValue(), keyboards[0].getAudioFeatures());
        assertEquals("Keyboard 1", keyboards[0].getName());
        singleKeyboard.setKeyboard(oldKeyboard);
        facade.setKeyboard(singleKeyboard);
        keyboards = facade.getKeyboards();
        assertNotNull(keyboards);
        assertTrue(keyboards.length > 0);
    }

    @Test
    public void testSetReader() throws ElkrommException {
        Reader[]    readers = facade.getReaders();
        
        assertNotNull(readers);
        assertTrue(readers.length > 0);

        Reader      oldReader = readers[0];
        boolean[]   associatedPartitions = ElkrommUtils.unpackPartitions((byte)0x01);
        Reader      newReader = new Reader(1, 
                                    new Input(1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input 1", Input.Delay.ID_30_SECS),
                                    new Input(2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input 2", Input.Delay.ID_20_SECS),
                                ElkrommFacade.Partition.P_ONE, ElkrommFacade.Partition.P_TWO, ElkrommFacade.Partition.P_THREE, ElkrommFacade.Partition.P_FOUR, Reader.Enablings.RE_MASKING.getValue(), "Reader 1");

        facade.setReader(newReader);
        readers = facade.getReaders();
        assertNotNull(readers);
        assertTrue(readers.length > 0);
        assertEquals(1, readers[0].getAddress());
        ElkrommTestUtils.testInput(newReader.getFirstInput(), readers[0].getFirstInput());
        ElkrommTestUtils.testInput(newReader.getSecondInput(), readers[0].getSecondInput());
        assertEquals(ElkrommFacade.Partition.P_ONE, readers[0].getLed1());
        assertEquals(ElkrommFacade.Partition.P_TWO, readers[0].getLed2());
        assertEquals(ElkrommFacade.Partition.P_THREE, readers[0].getLed3());
        assertEquals(ElkrommFacade.Partition.P_FOUR, readers[0].getLed4());
        assertEquals(Reader.Enablings.RE_MASKING.getValue(), readers[0].getEnablings());
        assertEquals("Reader 1", readers[0].getName());
        facade.setReader(oldReader);
        readers = facade.getReaders();
        assertNotNull(readers);
        assertTrue(readers.length > 0);
    }

    @Test
    public void testSetDayClassCommands() throws ElkrommException {
        TimeProgrammer      timeProgrammer = facade.getTimeProgrammer();

        assertNotNull(timeProgrammer);

        Command[]           workingDaysCommands = timeProgrammer.getWorkingDaysCommands();

        assertNotNull(workingDaysCommands);
        assertTrue(workingDaysCommands.length > 0);

        Command             oldCommand = workingDaysCommands[0];
        Command             newCommand = new Command(Command.Action.CA_ENABLE, (byte)3, Command.ObjectType.COT_USER, (byte)12, (byte)34);

        workingDaysCommands[0] = newCommand;
        facade.setDayClassCommands(new DayClassCommands(DayClassCommands.DayClass.DCCDC_WORKING_DAY, workingDaysCommands));
        timeProgrammer = facade.getTimeProgrammer();
        assertNotNull(timeProgrammer);
        workingDaysCommands = timeProgrammer.getWorkingDaysCommands();
        assertNotNull(workingDaysCommands);
        assertTrue(workingDaysCommands.length > 0);
        assertEquals(newCommand.getAction(), workingDaysCommands[0].getAction());
        assertEquals(newCommand.getObject(), workingDaysCommands[0].getObject());
        assertEquals(newCommand.getObjectType(), workingDaysCommands[0].getObjectType());
        assertEquals(newCommand.getHour(), workingDaysCommands[0].getHour());
        assertEquals(newCommand.getMinute(), workingDaysCommands[0].getMinute());
        workingDaysCommands[0] = oldCommand;
        facade.setDayClassCommands(new DayClassCommands(DayClassCommands.DayClass.DCCDC_WORKING_DAY, workingDaysCommands));
        timeProgrammer = facade.getTimeProgrammer();
        assertNotNull(timeProgrammer);
        workingDaysCommands = timeProgrammer.getWorkingDaysCommands();
        assertNotNull(workingDaysCommands);
        assertTrue(workingDaysCommands.length > 0);
    }

    @Test
    public void testSetUser() throws ElkrommException {
        Credential[]    users = facade.getUsers();

        assertNotNull(users);
        assertTrue(users.length > 0);

        Credential          oldUser = users[2];
        SingleCredential    sc = new SingleCredential((byte)3, new User(3, "Test user", Credential.Enabling.ENABLED, ElkrommUtils.unpackPartitions((byte)0x01)));

        facade.setUser(sc);
        users = facade.getUsers();
        assertNotNull(users);
        assertTrue(users.length > 0);
        assertEquals(3, users[2].getOrdinal());
        assertEquals("Test user", users[2].getName());
        assertEquals(Credential.Enabling.ENABLED, users[2].getEnabling());
        assertEquals(0x01, ElkrommUtils.packPartitions(users[2].getAssociatedPartitions()));
        sc.setCredential(oldUser);
        facade.setUser(sc);
        users = facade.getUsers();
        assertNotNull(users);
        assertTrue(users.length > 0);
    }

    @Test
    public void testSetKey() throws ElkrommException {
        Credential[]    keys = facade.getKeys();

        assertNotNull(keys);
        assertTrue(keys.length > 0);

        Credential          oldKey = keys[0];
        SingleCredential    sc = new SingleCredential((byte)3, new Key(3, "Test key", Credential.Enabling.ENABLED, Key.Specialization.KS_CHANGE_PARTITION_STATUS, ElkrommUtils.unpackPartitions((byte)0x01)));

        facade.setKey(sc);
        keys = facade.getKeys();
        assertNotNull(keys);
        assertTrue(keys.length > 0);
        assertEquals(3, keys[2].getOrdinal());
        assertEquals("Test key", keys[2].getName());
        assertEquals(Credential.Enabling.ENABLED, keys[2].getEnabling());
        assertEquals(Key.Specialization.KS_CHANGE_PARTITION_STATUS, ((Key)keys[2]).getSpecialization());
        assertEquals(0x01, ElkrommUtils.packPartitions(keys[2].getAssociatedPartitions()));
        sc.setCredential(oldKey);
        facade.setKey(sc);
        keys = facade.getKeys();
        assertNotNull(keys);
        assertTrue(keys.length > 0);
    }

    @AfterClass
    public static void shutdownTests() throws UnknownHostException
    {
        try {
            facade.logout();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
        
        try {
            facade.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
