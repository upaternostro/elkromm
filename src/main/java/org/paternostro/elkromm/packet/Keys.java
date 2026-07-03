package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class Keys extends ElkrommPacket {
    public Keys(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.KEYS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets == 5 && dataLength > 0) : "Keys: totalPackets: expected 0 or 5, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && index <= 5 && dataLength > 0) : "Keys: index: expected 0...5, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "Keys: data: mismatch in data length";
    }
}
