package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class C200bParameters extends ElkrommPacket {
    public C200bParameters(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.C200B.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets == 1 && dataLength > 0) : "C200b: totalPackets: expected 0 or 1, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && index <= 1 && dataLength > 0) : "C200b: index: expected 0...1, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "C200b: data: mismatch in data length";
    }
}
