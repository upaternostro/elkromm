package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class Expansions extends ElkrommPacket {
    public Expansions(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.EXPANSIONS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets > 0 && dataLength > 0) : "Expansions: totalPackets: expected 0 or 12, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && dataLength > 0) : "Expansions: index: expected 0...12, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "Expansions: data: mismatch in data length";
    }
}
