package org.paternostro.elkromm;

import java.util.Queue;

import org.paternostro.elkromm.packet.ElkrommPacket;

/**
 * A {@link Queue} of outgoing {@link ElkrommPacket}s, with a convenience
 * method that splits an arbitrarily long payload into as many correctly
 * framed, indexed packets as needed.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public interface PacketQueue extends Queue<ElkrommPacket> {
    /**
     * Splits {@code payload} into one or more {@link ElkrommPacket}s for
     * {@code command} (respecting {@link ElkrommFacade#MAX_DATA_LENGTH}) and
     * enqueues them, in order, ready to be sent.
     *
     * @param command the command these packets belong to
     * @param plantCode12 first two BCD digits of the plant code, as used in the packet header
     * @param plantCode34 last two BCD digits of the plant code, as used in the packet header
     * @param payload the full payload to split and enqueue
     * @throws ElkrommException if the payload cannot be framed
     */
    public void enqueuePayload(ElkronCommand command, byte plantCode12, byte plantCode34, byte[] payload) throws ElkrommException;
}
