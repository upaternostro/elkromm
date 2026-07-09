package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class Keypads extends ElkrommPacket {
    public Keypads(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.KEYPADS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets >= 0 && dataLength > 0) : "Keypads: totalPackets: expected 0 or more, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && dataLength > 0) : "Keypads: index: expected 0 or more, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "Keypads: data: mismatch in data length";
    }
}
