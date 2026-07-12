package org.paternostro.elkromm;

import java.util.Queue;

import org.paternostro.elkromm.packet.ElkrommPacket;

public interface PacketQueue extends Queue<ElkrommPacket> {
    public void enqueuePayload(ElkrommPacket packet, byte[] payload) throws ElkrommException;
}
