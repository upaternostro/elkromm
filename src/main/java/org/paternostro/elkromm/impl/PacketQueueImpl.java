package org.paternostro.elkromm.impl;

import java.util.Arrays;
import java.util.LinkedList;

import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.PacketQueue;
import org.paternostro.elkromm.packet.ElkrommPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link PacketQueue} implementation, backed by a plain
 * {@link LinkedList}. {@link #enqueuePayload} does the actual work of
 * splitting an arbitrary-length payload into as many
 * {@link ElkrommFacade#MAX_DATA_LENGTH}-sized, correctly indexed packets
 * as needed, rolling back any partially-enqueued packets if allocation
 * fails partway through.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PacketQueueImpl extends LinkedList<ElkrommPacket> implements PacketQueue
{
    /** Logger used to dump outgoing payloads at debug level. */
    public static final Logger logger = LoggerFactory.getLogger(PacketQueueImpl.class);

    @Override
    public void enqueuePayload(ElkronCommand command, byte plantCode12, byte plantCode34, byte[] payload) throws ElkrommException
    {
        byte[]  payloadPart;

        if (logger.isDebugEnabled()) ElkrommUtils.dumpPayload(command, payload);

        // split payload
        int totalPackets = ((payload == null ? 0 : payload.length) - 1) / ElkrommFacade.MAX_DATA_LENGTH; // numero di pacchetti totali (base 0), meno uno perché 140 byte entrano tutti nel primo pacchetto
        int from = 0;
        int to;
        int index = 0;

        if (totalPackets < 0) {
            // fix per payload vuoto/nullo
            totalPackets = 0;
        }

        try {
            while (index <= totalPackets) {
                to = Math.min(from + ElkrommFacade.MAX_DATA_LENGTH, payload == null ? 0 : payload.length);
                payloadPart = payload != null ? Arrays.copyOfRange(payload, from, to) : null;
                add(ElkrommPacket.packetFactoryAllocate(command, plantCode12, plantCode34, totalPackets, index, payload == null ? 0 : payloadPart.length, payloadPart));
                from = to;
                index++;
            }
        } catch (ElkrommException e) {
            logger.error("Error allocating packet", e);

            // rollback, if any
            while (index-- > 0) this.removeLast();

            throw new ElkrommException("Error allocating packet", e);
        }
    }
}
