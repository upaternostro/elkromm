package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class Readers extends ElkrommPacket {
    public Readers(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.READERS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets >= 0 && dataLength > 0) : "Readers: totalPackets: expected 0 or more, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && dataLength > 0) : "Readers: index: expected 0 or more, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "Readers: data: mismatch in data length";
    }
}
