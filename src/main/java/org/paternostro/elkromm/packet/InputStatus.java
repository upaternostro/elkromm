package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class InputStatus extends ElkrommPacket {
    public InputStatus(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.INPUT_STATUS.getValue(), null);

        assert totalPackets == 0 : "InputStatus: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "InputStatus: index: expected 0, found " + index;
        assert dataLength == 0 : "InputStatus: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "InputStatus: data: mismatch in data length";
    }
}
