package org.paternostro.elkromm.emulator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.paternostro.elkromm.dto.C200bParameters;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ConfigTest {
    @Test
    public void testFactory() {
        Object factory = Config.getInstance();

        assertNotNull(factory);
        assertTrue(factory instanceof InjectableConfig);
    }

    @Test
    public void testFactorySingleton() {
        Object factory = Config.getInstance();

        assertNotNull(factory);
        assertTrue(factory instanceof InjectableConfig);

        Object factory2 = Config.getInstance();

        assertNotNull(factory2);
        assertTrue(factory2 instanceof InjectableConfig);
        assertSame(factory, factory2);
    }

    @Test
    public void testFromPropertiesReadsValues()
    {
        Properties  properties = new Properties();

        properties.setProperty(Config.K_PLANT_CODE, "12345678");
        properties.setProperty(Config.K_TECHNICAL_CODE, "987654");
        properties.setProperty(Config.K_EXPANSIONS, "2");

        InjectableConfig  config = (InjectableConfig)Config.getInstance();

        config.setProperties(properties);

        assertEquals(12345678, config.getPlantCode());
        assertEquals(987654, config.getTechnicalCode());
        assertEquals(2, config.getExpansions());
    }

    @Test
    public void testFromPropertiesMissingKeysTakeDefaults()
    {
        InjectableConfig  config = (InjectableConfig)Config.getInstance();

        config.setProperties(new Properties());

        assertEquals(Integer.parseInt(Config.D_PLANT_CODE), config.getPlantCode());
        assertEquals(Integer.parseInt(Config.D_TECHNICAL_CODE), config.getTechnicalCode());
        assertEquals(Integer.parseInt(Config.D_PORT), config.getPort());
    }

    @Test
    public void testFromPropertiesIsACopy()
    {
        Properties  properties = new Properties();

        properties.setProperty(Config.K_PLANT_CODE, "11111111");

        InjectableConfig  config = (InjectableConfig)Config.getInstance();

        config.setProperties(properties);

        properties.setProperty(Config.K_PLANT_CODE, "22222222");

        assertEquals(11111111, config.getPlantCode());
    }

    @Test
    public void testFromPropertiesKeepsInheritedDefaults()
    {
        Properties  defaults = new Properties();

        defaults.setProperty(Config.K_PLANT_CODE, "33333333");

        InjectableConfig  config = (InjectableConfig)Config.getInstance();

        config.setProperties(new Properties(defaults));

        assertEquals(33333333, config.getPlantCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromNullPropertiesFails()
    {
        InjectableConfig  config = (InjectableConfig)Config.getInstance();

        config.setProperties(null);
    }

    private static final String SEQUENCE_HEX = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031";

    private static byte[] sequence()
    {
        byte[]  retval = new byte[C200bParameters.RESERVED_SIZE];

        for (int i = 0; i < retval.length; i++) {
            retval[i] = (byte)i;
        }

        return retval;
    }

    @Test
    public void testC200bReservedEmptyIsZero()
    {
        assertArrayEquals(new byte[C200bParameters.RESERVED_SIZE], Config.parseC200bReserved(""));
    }

    @Test
    public void testC200bReservedHex()
    {
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX));
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX.toUpperCase()));
    }

    @Test
    public void testC200bReservedIgnoresSeparators()
    {
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX.replaceAll("(..)", "$1 ").trim()));
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX.replaceAll("(..)", "$1:").replaceAll(":$", "")));
    }

    @Test
    public void testC200bReservedMalformedIsZero()
    {
        byte[]  zeros = new byte[C200bParameters.RESERVED_SIZE];

        assertArrayEquals(zeros, Config.parseC200bReserved(SEQUENCE_HEX.substring(2)));
        assertArrayEquals(zeros, Config.parseC200bReserved(SEQUENCE_HEX + "00"));
        assertArrayEquals(zeros, Config.parseC200bReserved("zz" + SEQUENCE_HEX.substring(2)));
    }
}
