package org.paternostro.elkromm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.serializer.ElkrommSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class RoundTripIT {
    public static final Logger logger       = LoggerFactory.getLogger(RoundTripIT.class);

    private static ITConfig             config;
    private static ElkrommFactory       factory;
    private static ElkrommFacadeImpl    facade;

    @BeforeClass
    public static void initTests() throws IOException
    {
        config = ITConfig.getInstance();

        assertNotNull(config);

        if (config.areITEnabled()) {
            factory = ElkrommFactory.getFactory();

            assertNotNull(factory);

            facade = (ElkrommFacadeImpl)factory.getElkrommFacade(InetAddress.getByName(config.getHost()), config.getPort(), config.getPlantCode());

            assertNotNull(facade);
            assertEquals(facade.getStatus(), Status.ST_DISCONNECTED);

            try {
                facade.connect();
                assertEquals(facade.getStatus(), Status.ST_CONNECTED);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                assertTrue(false);
            }
            
            try {
                facade.login(config.getPlantCode(), config.getTechnicalPin());
                assertEquals(facade.getStatus(), Status.ST_LOGGED_IN);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                assertTrue(false);
            }
        }
    }

    interface Validate {
        boolean validate(int index);
    }

    interface Patch {
        void patchData(int index, byte[] data, byte[] roundTripVerify);
    }

    private void compareArrays(byte[] data, byte[] roundTripVerify, Validate validator, Patch patcher)
    {
        boolean differences = false;
        int     startIndex = -1;
        byte    lastData = 0;
        byte    lastRTV = 0;

        assertNotNull(roundTripVerify);
        assertEquals(data.length, roundTripVerify.length);

        for (int i = 0; i < data.length - 4; i++) {
            if (validator != null && !validator.validate(i)) continue;

            if (patcher != null) patcher.patchData(i, data, roundTripVerify);

            if (data[i] != roundTripVerify[i]) {
                differences = true;

                if (startIndex == -1) {
                    startIndex = i;
                    lastData = data[i];
                    lastRTV = roundTripVerify[i];
                } else {
                    if (data[i] != lastData || roundTripVerify[i] != lastRTV) {
                        if (startIndex == i - 1) {
                        logger.error(String.format("Different byte!  0x%08x           : 0x%02x <-> 0x%02x", startIndex, lastData, lastRTV));
                    } else {
                        logger.error(String.format("Different bytes! 0x%08x-0x%08x: 0x%02x <-> 0x%02x", startIndex, i - 1, lastData, lastRTV));
                        }

                        startIndex = i;
                        lastData = data[i];
                        lastRTV = roundTripVerify[i];
                    }
                }
            } else {
                if (startIndex != -1) {
                    if (startIndex == i - 1) {
                        logger.error(String.format("Different byte!  0x%08x           : 0x%02x <-> 0x%02x", startIndex, lastData, lastRTV));
                    } else {
                        logger.error(String.format("Different bytes! 0x%08x-0x%08x: 0x%02x <-> 0x%02x", startIndex, i - 1, lastData, lastRTV));
                    }

                    startIndex = -1;
                }
            }
        }

        // for (int i = 0; i < data.length - 4; i++) {
        //     if (data[i] != roundTripVerify[i]) {
        //         logger.error(String.format("Byte diverso! 0x%08x: 0x%02x <-> 0x%02x", i, data[i], roundTripVerify[i]));
        //     }
        // }

        assertTrue(!differences);
    }

    private <T> void roundTripIT(ElkronCommand command, ElkrommSerializer<T> serializer, Validate validator, Patch patcher) throws ElkrommException
    {
        assertNotNull(serializer);

        byte[]  data = facade.getData(command);

        assertNotNull(data);
        assertTrue(data.length > 0);

        T       dto = serializer.deserialize(data);

        assertNotNull(dto);
        
        if (dto.getClass().isArray()) {
            StringBuffer sb = new StringBuffer("DTO[]: [");

            for (Object pivot : ((Object[])dto)) {
                sb.append(pivot).append(", ");
            }

            sb.setLength(sb.length() - 2);
            sb.append("]");

            logger.debug(sb.toString());
        } else {
            logger.debug("DTO: " + dto);
        }

        byte[]  roundTripVerify = serializer.serialize(dto);

        compareArrays(data, roundTripVerify, validator, patcher);

        ElkrommUtils.dumpPayload(command, roundTripVerify);
    }

    @Test
    public void partitionsAndAreasRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.PARTITIONS_AND_AREAS, factory.getAreasAndPartitionsSerializer(), null, null);
        }
    }

    @Test
    public void systemStatusRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.SYSTEM_STATUS, factory.getSystemStatusSerializer(), null, null);
        }
    }

    @Test
    public void peripheralUnitsRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.PERIPHERAL_UNITS_ADDRESSES, factory.getPeripheralUnitsSerializer(), null, null);
        }
    }

    @Test
    public void checksumsRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.CHECKSUM, factory.getChecksumsSerializer(), null, null);
        }
    }

    @Test
    public void usersRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.USERS, factory.getUsersSerializer(), null, null);
        }
    }

    @Test
    public void keysRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.KEYS, factory.getKeysSerializer(), null, null);
        }
    }

    @Test
    public void phoneNumbersSendingCodesRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.PHONE_NUMBERS, factory.getPhoneNumbersSendingCodesSerializer(), null, null);
        }
    }

    @Test
    public void phoneParametersRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.PHONE_PARAMETERS, factory.getPhoneParametersSerializer(), null, null);
        }
    }

    @Test
    public void pstnGsmRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.PSTN_GSM, factory.getPSTNGSMSerializer(), null, null);
        }
    }

    @Test
    public void readersRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.READERS, factory.getReadersSerializer(), null, null);
        }
    }

    @Test
    public void smssRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.SMS, factory.getSMSsSerializer(), null, null);
        }
    }

    @Test
    public void timeProgrammerRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.TIME_PROGRAMMER, factory.getTimeProgrammerSerializer(), null, null);
        }
    }

    @Test
    public void c200BRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.C200B, factory.getC200bParametersSerializer(), null, null);
        }
    }

    @Test 
    public void expansionsRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.EXPANSIONS, factory.getExpansionsSerializer(), index -> index % 559 != 557 && index % 559 != 558, (index, d, rtv) -> {
                int inputNum = ((index % 559) - 7) / 38;

                if (inputNum < 8 && ((index % 559) - 7) % 38 == 3) {
                    d[index] &= ~0x10;
                }
            });
        }
    }

    @Test
    public void userEnablingsRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.USER_ENABLINGS, factory.getUserEnablingsSerializer(), null, null);
        }
    }

    @Test
    public void keypadsRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.KEYPADS, factory.getKeyboardsSerializer(), null, null);
        }
    }

    @Test
    public void parametersEnablingsRoundTripIT() throws ElkrommException
    {
        if (config.areITEnabled()) {
            roundTripIT(ElkronCommand.PARAMETERS_ENABLINGS, factory.getParametersEnablingsSerializer(), null, null);
        }
    }

    @AfterClass
    public static void shutdownTests() throws UnknownHostException
    {
        if (facade != null) {
            try {
                facade.logout();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                assertTrue(false);
            } finally {
                try {
                    facade.disconnect();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    assertTrue(false);
                }
            }
        }
    }
}
