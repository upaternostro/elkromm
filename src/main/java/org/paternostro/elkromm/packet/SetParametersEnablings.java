package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class SetParametersEnablings extends ElkrommPacket {
    public SetParametersEnablings(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_PARAMETERS_ENABLINGS.getValue(), data);

        assert totalPackets == 0 : "SetParametersEnablings: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "SetParametersEnablings: index: expected 0, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength == 30 && data != null && data.length == dataLength) : "SetParametersEnablings: data: mismatch in data length";
    }
}
