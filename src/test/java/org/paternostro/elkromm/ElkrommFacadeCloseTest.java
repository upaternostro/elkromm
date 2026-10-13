package org.paternostro.elkromm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade.Status;
import org.paternostro.elkromm.emulator.ClientConnection;
import org.paternostro.elkromm.emulator.Config;
import org.paternostro.elkromm.emulator.Model;
import org.paternostro.elkromm.impl.ElkrommFacadeImpl;
import org.paternostro.mock.ipc.Channel;
import org.paternostro.mock.ipc.Endpoint;
import org.paternostro.mock.ipc.EndpointFactory;

/**
 * Tests the lifecycle contract of {@link ElkrommFacade#disconnect()} and
 * {@link ElkrommFacade#close()}, using the bundled emulator over pipe
 * endpoints.
 * <p>
 * Copyright Ugo Paternostro 2026. Licensed under the EUPL-1.2 or later.
 */
public class ElkrommFacadeCloseTest {
    /** Decorates an {@link Endpoint} counting {@code close()} calls and optionally failing writes. */
    private static class SpyEndpoint implements Endpoint {
        private final Endpoint delegate;
        volatile boolean failWrites = false;
        int closeCalls = 0;

        SpyEndpoint(Endpoint delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isConnected() {
            return delegate.isConnected();
        }

        @Override
        public void connect() throws IOException {
            delegate.connect();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FilterOutputStream(delegate.getOutputStream()) {
                @Override
                public void write(int b) throws IOException {
                    if (failWrites) throw new IOException("Injected write failure");
                    out.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    if (failWrites) throw new IOException("Injected write failure");
                    out.write(b, off, len);
                }
            };
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public void close() throws IOException {
            closeCalls++;
            delegate.close();
        }
    }

    private SpyEndpoint     endpoint;
    private ElkrommFacade   facade;

    @Before
    public void setUp() throws IOException
    {
        Channel c2s = new Channel();
        Channel s2c = new Channel();
        ClientConnection server = new ClientConnection(EndpointFactory.getFactory().getPipeEndpoint(s2c, c2s), new Model());

        server.start();

        endpoint = new SpyEndpoint(EndpointFactory.getFactory().getPipeEndpoint(c2s, s2c));
        facade = ElkrommFactory.getFactory().getElkrommFacade(endpoint, Config.getInstance().getPlantCode());

        assertNotNull(facade);
        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
    }

    private void connect() throws ElkrommException
    {
        facade.connect();
        assertEquals(Status.ST_CONNECTED, facade.getStatus());
    }

    private void login() throws ElkrommException
    {
        connect();
        facade.login(Config.getInstance().getPlantCode(), Config.getInstance().getTechnicalCode());
        assertEquals(Status.ST_LOGGED_IN, facade.getStatus());
    }

    @Test
    public void disconnectFromConnectedClosesEndpoint() throws ElkrommException
    {
        connect();
        facade.disconnect();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(1, endpoint.closeCalls);
    }

    @Test
    public void disconnectFromDisconnectedIsNoOp() throws ElkrommException
    {
        facade.disconnect();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(0, endpoint.closeCalls);

        connect();
        facade.disconnect();
        facade.disconnect();

        assertEquals(1, endpoint.closeCalls);
    }

    @Test
    public void disconnectWhileLoggedInFails() throws ElkrommException
    {
        login();

        try {
            facade.disconnect();
            fail("disconnect() must fail while logged in");
        } catch (AssertionError e) {
            // Wrong status (still logged in)
        }

        // The session is untouched and can be closed properly
        assertEquals(Status.ST_LOGGED_IN, facade.getStatus());
        assertEquals(0, endpoint.closeCalls);

        facade.close();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
    }

    @Test
    public void disconnectNotInitializedFails() throws ElkrommException
    {
        ElkrommFacade notInitialized = new ElkrommFacadeImpl();

        assertEquals(Status.ST_NOT_INITIALIZED, notInitialized.getStatus());

        try {
            notInitialized.disconnect();
            fail("disconnect() must fail when not initialized");
        } catch (AssertionError e) {
            // Wrong status (not initialized)
        }
    }

    @Test
    public void closeFromLoggedInLogsOutAndDisconnects() throws ElkrommException
    {
        login();
        facade.close();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(1, endpoint.closeCalls);
        assertFalse(endpoint.isConnected()); // needs mock-ipc 0.3+: closing a pipe endpoint really closes it
    }

    @Test
    public void closeFromConnectedDisconnects() throws ElkrommException
    {
        connect();
        facade.close();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(1, endpoint.closeCalls);
    }

    @Test
    public void closeNeverConnectedReleasesEndpoint() throws ElkrommException
    {
        // Pipe endpoints are connected by construction: close() must release them even if connect() was never called
        facade.close();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(1, endpoint.closeCalls);
        assertFalse(endpoint.isConnected()); // needs mock-ipc 0.3+
    }

    @Test
    public void closeIsIdempotent() throws ElkrommException
    {
        login();
        facade.close();
        facade.close();
        facade.disconnect();

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertFalse(endpoint.isConnected()); // needs mock-ipc 0.3+
    }

    @Test
    public void closeNotInitializedIsNoOp() throws ElkrommException
    {
        new ElkrommFacadeImpl().close();
    }

    @Test
    public void closeDisconnectsEvenIfLogoutFails() throws ElkrommException
    {
        login();
        endpoint.failWrites = true;

        try {
            facade.close();
            fail("close() must report the logout failure");
        } catch (ElkrommException e) {
            // Logout failed: this is the primary failure
        }

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(1, endpoint.closeCalls);
    }

    @Test
    public void tryWithResourcesClosesFacade() throws ElkrommException
    {
        try (ElkrommFacade f = facade) {
            f.connect();
            f.login(Config.getInstance().getPlantCode(), Config.getInstance().getTechnicalCode());
            assertEquals(Status.ST_LOGGED_IN, f.getStatus());
        }

        assertEquals(Status.ST_DISCONNECTED, facade.getStatus());
        assertEquals(1, endpoint.closeCalls);
    }

    @After
    public void tearDown() throws ElkrommException
    {
        facade.close();
    }
}
