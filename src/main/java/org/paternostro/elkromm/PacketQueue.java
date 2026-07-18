package org.paternostro.elkromm;

import java.util.Queue;

import org.paternostro.elkromm.packet.ElkrommPacket;

public interface PacketQueue extends Queue<ElkrommPacket> {
    public void enqueuePayload(ElkronCommand command, byte plantCode12, byte plantCode34, byte[] payload) throws ElkrommException;
}
