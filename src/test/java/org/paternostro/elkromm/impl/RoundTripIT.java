package org.paternostro.elkromm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.dto.Expansion;
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
            if (!validator.validate(i)) continue;

            patcher.patchData(i, data, roundTripVerify);

            if (data[i] != roundTripVerify[i]) {
                differences = true;

                if (startIndex == -1) {
                    startIndex = i;
                    lastData = data[i];
                    lastRTV = roundTripVerify[i];
                } else {
                    if (data[i] != lastData || roundTripVerify[i] != lastRTV) {
                        if (startIndex == i - 1) {
                            logger.error(String.format("Byte diverso! 0x%08x: 0x%02x <-> 0x%02x", startIndex, lastData, lastRTV));
                        } else {
                            logger.error(String.format("Bytes diversi! 0x%08x-0x%08x: 0x%02x <-> 0x%02x", startIndex, i - 1, lastData, lastRTV));
                        }

                        startIndex = i;
                        lastData = data[i];
                        lastRTV = roundTripVerify[i];
                    }
                }
            } else {
                if (startIndex != -1) {
                    if (startIndex == i - 1) {
                        logger.error(String.format("Byte diverso! 0x%08x: 0x%02x <-> 0x%02x", startIndex, lastData, lastRTV));
                    } else {
                        logger.error(String.format("Bytes diversi! 0x%08x-0x%08x: 0x%02x <-> 0x%02x", startIndex, i - 1, lastData, lastRTV));
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

    @Test 
    public void expansionsRoundTripIT() throws UnknownHostException, IOException, ElkrommException
    {
        if (config.areITEnabled()) {
            ElkrommSerializer<Expansion[]>  expansionsSerializer = factory.getExpansionsSerializer();

            assertNotNull(expansionsSerializer);

            byte[]                          data = facade.getData(ElkronCommand.EXPANSIONS);

            assertNotNull(data);
            assertTrue(data.length > 0);

            Expansion[]                     expansions = expansionsSerializer.deserialize(data);

            assertNotNull(expansions);
            assertTrue(expansions.length > 0);

            logger.debug("Expansions: " + Arrays.toString(expansions));

            byte[]                          roundTripVerify = expansionsSerializer.serialize(expansions);

            compareArrays(data, roundTripVerify, index -> index % 559 != 557 && index % 559 != 558, (index, d, rtv) -> {
                int inputNum = ((index % 559) - 7) / 38;

                if (inputNum < 8 && ((index % 559) - 7) % 38 == 3) {
                    d[index] &= ~0x10;
                    // logger.debug("Patching " + index + " - Input num: " + inputNum);
                }
            });

            ElkrommUtils.dumpPayload(ElkronCommand.EXPANSIONS, roundTripVerify);
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
