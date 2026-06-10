package org.paternostro.elkromm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade.Status;

public class ElkrommFacadeTest {
    @Test
    public void testFacade() throws UnknownHostException {
        ElkrommFactory factory = ElkrommFactory.getFactory();

        assertNotNull(factory);

        ElkrommFacade facade = factory.getFacade(InetAddress.getLocalHost(), 8030, 12345678);

        assertNotNull(facade);
        assertEquals(facade.getStatus(), Status.ST_DISCONNECTED);

        try {
            facade.ping();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not connected)
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
    public void testStatus() throws UnknownHostException {
        ElkrommFactory factory = ElkrommFactory.getFactory();

        assertNotNull(factory);

        ElkrommFacade facade = factory.getFacade(InetAddress.getLocalHost(), 8030, 12345678);

        assertNotNull(facade);
        assertEquals(facade.getStatus(), Status.ST_DISCONNECTED);

        try {
            facade.getSystemStatus();
            assertTrue(false);
        } catch (AssertionError e) {
            // Wrong status (not connected)
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
    }
}
