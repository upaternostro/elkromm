package org.paternostro.elkromm.emulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.mock.ipc.Channel;
import org.paternostro.mock.ipc.EndpointFactory;

/**
 * An emulator configured by hand, without relying on the {@code elkron.properties} found on the
 * classpath, so different configurations can coexist in the same JVM.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class InjectedConfigTest {
    private static final int PLANT_CODE      = 12345678;
    private static final int TECHNICAL_CODE  = 987654;

    private Model           model;
    private ElkrommFacade   facade;

    private static Config config(int keyboards, int expansions, int partitions)
    {
        Properties  properties = new Properties();

        properties.setProperty(Config.K_PLANT_CODE, Integer.toString(PLANT_CODE));
        properties.setProperty(Config.K_TECHNICAL_CODE, Integer.toString(TECHNICAL_CODE));
        properties.setProperty(Config.K_KEYBOARDS, Integer.toString(keyboards));
        properties.setProperty(Config.K_EXPANSIONS, Integer.toString(expansions));
        properties.setProperty(Config.K_PARTITIONS, Integer.toString(partitions));

        InjectableConfig    config = (InjectableConfig)Config.getInstance();

        config.setProperties(properties);

        return config;
    }

    @Before
    public void setUp() throws IOException, ElkrommException
    {
        Channel c2s = new Channel();
        Channel s2c = new Channel();

        config(2, 1, 3);
        model = new Model();

        ClientConnection    server = new ClientConnection(EndpointFactory.getFactory().getPipeEndpoint(s2c, c2s), model);

        server.start();

        facade = ElkrommFactory.getFactory().getElkrommFacade(EndpointFactory.getFactory().getPipeEndpoint(c2s, s2c), PLANT_CODE);
        facade.setDelay(0);
    }

    @After
    public void tearDown() throws ElkrommException
    {
        facade.close();
    }

    @Test
    public void modelFollowsItsConfig()
    {
        // assertSame(config, model.getConfig());
        assertEquals(2, model.getKeyboards().length);
        assertEquals(2, model.getExpansions().length);       // the main board plus one expansion
        assertEquals(3, model.getAreasAndPartitions().getPartitionNum());
    }

    @Test(expected = IllegalArgumentException.class)
    public void restoreWithoutPropertiesFails()
    {
        InjectableConfig    config = (InjectableConfig)Config.getInstance();

        config.restore();
    }

    @Test
    public void copyProperties()
    {
        InjectableConfig    config = (InjectableConfig)Config.getInstance();
        Properties          p = new Properties();

        p.setProperty(String.format(Config.K_AREA_NAME, 1), "aValue");
        config.setProperties(p);
        assertEquals("aValue", config.getAreaName(1));
        p.setProperty(String.format(Config.K_AREA_NAME, 1), "anotherValue");
        // Config does not change as it copied properties on setter
        assertEquals("aValue", config.getAreaName(1));
    }

    @Test
    public void snapshot()
    {
        InjectableConfig    config = (InjectableConfig)Config.getInstance();
        Properties          p1 = new Properties();

        p1.setProperty(String.format(Config.K_AREA_NAME, 1), "aValue");
        config.setProperties(p1);
        assertEquals("aValue", config.getAreaName(1));

        config.snapshot();

        Properties          p2 = new Properties();

        p2.setProperty(String.format(Config.K_AREA_NAME, 1), "anotherValue");
        config.setProperties(p2);
        assertEquals("anotherValue", config.getAreaName(1));

        config.restore();
        assertEquals("aValue", config.getAreaName(1));
    }

    @Test
    public void loginWithInjectedCodes() throws ElkrommException
    {
        facade.connect();
        facade.login(PLANT_CODE, TECHNICAL_CODE);

        assertEquals(Status.ST_LOGGED_IN, facade.getStatus());
    }

    @Test
    public void loginWithWrongTechnicalCodeIsRefused() throws ElkrommException
    {
        facade.connect();

        try {
            facade.login(PLANT_CODE, TECHNICAL_CODE + 1);
            fail("The emulator must refuse a wrong technical code");
        } catch (AssertionError e) {
            // No ACK received: the emulator answered with a NAK
        }

        assertEquals(Status.ST_CONNECTED, facade.getStatus());
    }
}
