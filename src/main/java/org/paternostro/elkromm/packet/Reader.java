package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class Reader extends ElkrommPacket {
    public Reader(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.READER_PROGRAMMING.getValue(), data);

        assert totalPackets == 0 : "Reader: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Reader: index: expected 0, found " + index;
        assert dataLength == 113 && data != null && data.length == dataLength : "Reader: data: mismatch in data length";
    }
}
