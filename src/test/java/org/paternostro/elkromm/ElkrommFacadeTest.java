package org.paternostro.elkromm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.emulator.ClientConnection;
import org.paternostro.elkromm.emulator.Model;
import org.paternostro.mock.ipc.Channel;
import org.paternostro.mock.ipc.EndpointFactory;

public class ElkrommFacadeTest {
    private static ClientConnection server;
    private static ElkrommFacade    facade;

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
    public void testFacade() throws UnknownHostException, IOException {
        try {
            facade.ping();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not connected)
        } catch (ElkrommException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            facade.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            facade.ping();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not logged in)
        } catch (ElkrommException e) {
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

    @Test
    public void testStatus() throws UnknownHostException, IOException {
        try {
            facade.getSystemStatus();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not connected)
        } catch (ElkrommException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            facade.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            facade.getSystemStatus();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not logged in)
        } catch (ElkrommException e) {
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

        try {
            facade.getSystemStatus();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            facade.logout();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            facade.getSystemStatus();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not logged in)
        } catch (ElkrommException e) {
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

        try {
            facade.getSystemStatus();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not connected)
        } catch (ElkrommException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testPing() throws UnknownHostException, IOException
    {
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

        try {
            facade.ping();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

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
