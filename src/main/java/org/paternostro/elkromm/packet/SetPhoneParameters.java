package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class SetPhoneParameters extends ElkrommPacket {
    public SetPhoneParameters(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_PHONE_PARAMETERS.getValue(), data);

        assert totalPackets == 0 : "SetPhoneParameters: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "SetPhoneParameters: index: expected 0, found " + index;
        assert dataLength == 20 && data != null && data.length == dataLength : "SetPhoneParameters: data: mismatch in data length";
    }
}
