package org.paternostro.elkromm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
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
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
import org.paternostro.elkromm.dto.User;
import org.paternostro.elkromm.emulator.ClientConnection;
import org.paternostro.elkromm.emulator.Model;
import org.paternostro.mock.ipc.Channel;
import org.paternostro.mock.ipc.EndpointFactory;

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

    // setters

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
        
        // try {
        //     facade.disconnect();
        // } catch (Exception e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        //     assertTrue(false);
        // }
    }
}
