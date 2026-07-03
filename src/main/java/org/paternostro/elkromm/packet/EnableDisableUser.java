package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class EnableDisableUser extends ElkrommPacket {
    public EnableDisableUser(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, dataLength, ElkronCommand.ENABLE_DISABLE_USER.getValue(), data);

        assert totalPackets == 0 : "EnableDisableUser: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "EnableDisableUser: index: expected 0, found " + index;
        assert dataLength == 2 : "EnableDisableUser: dataLength: expected 2, found " + dataLength;
        assert dataLength == 2 && data != null && data.length == 2 : "EnableDisableUser: data: mismatch in data length";
    }
}
