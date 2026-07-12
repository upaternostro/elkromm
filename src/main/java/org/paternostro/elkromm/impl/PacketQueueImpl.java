package org.paternostro.elkromm.impl;

import java.util.Arrays;
import java.util.LinkedList;

import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.PacketQueue;
import org.paternostro.elkromm.packet.ElkrommPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketQueueImpl extends LinkedList<ElkrommPacket> implements PacketQueue
{
    public static final Logger logger = LoggerFactory.getLogger(PacketQueueImpl.class);

    @Override
    public void enqueuePayload(ElkrommPacket packet, byte[] payload) throws ElkrommException
    {
        byte[]  payloadPart;

        if (logger.isDebugEnabled()) ElkrommUtils.dumpPayload(packet.getCommand(), payload);

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
                add(ElkrommPacket.packetFactoryAllocate(packet.getCommand(), packet.getPlantCode12(), packet.getPlantCode34(), totalPackets, index, payload == null ? 0 : payloadPart.length, payloadPart));
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
