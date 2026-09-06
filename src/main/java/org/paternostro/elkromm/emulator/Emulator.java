package org.paternostro.elkromm.emulator;

import java.net.ServerSocket;
import java.net.Socket;

import org.paternostro.mock.ipc.EndpointFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Emulator 
{
    public static final Logger logger = LoggerFactory.getLogger(Emulator.class);

    public static void main( String[] args)
    {
        Config          config = Config.getInstance();
        Model           model = new Model();
        ServerSocket    serverSocket = null;

        try {
            serverSocket = new ServerSocket(config.getPort());
            logger.info("Server started on port " + config.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Wait for a client to connect
                logger.info("Client connected");
                
                ClientConnection    clientHandler = new ClientConnection(EndpointFactory.getFactory().getSocketEndpoint(clientSocket), model);

                clientHandler.start();
            }
        } catch (Exception e) {
            logger.error("Exception accepting connections", e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    logger.warn("Exception closing socket", e);
               }
            }
        }
    }
}
