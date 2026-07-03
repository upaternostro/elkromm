package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class ExcludeIncludeInput extends ElkrommPacket {
    public ExcludeIncludeInput(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.EXCLUDE_INCLUDE_INPUT.getValue(), null);

        assert totalPackets == 0 : "ExcludeIncludeInput: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "ExcludeIncludeInput: index: expected 0, found " + index;
        assert dataLength == 0 : "ExcludeIncludeInput: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "ExcludeIncludeInput: data: mismatch in data length";
    }
}
