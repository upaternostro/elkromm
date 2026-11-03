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
}
