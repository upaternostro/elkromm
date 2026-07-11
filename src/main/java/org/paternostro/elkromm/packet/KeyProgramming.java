package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class KeyProgramming extends ElkrommPacket {
    public KeyProgramming(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.KEY_PROGRAMMING.getValue(), data);

        assert totalPackets == 0 : "Key: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Key: index: expected 0, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "Key: data: mismatch in data length";
    }
}
