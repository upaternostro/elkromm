package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class SetPSTNGSM extends ElkrommPacket {
    public SetPSTNGSM(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_PSTN_GSM.getValue(), data);

        assert totalPackets == 0 : "SetPSTNGSM: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "SetPSTNGSM: index: expected 0, found " + index;
        assert dataLength == 21 && data != null && data.length == dataLength : "SetPSTNGSM: data: mismatch in data length";
    }
}
