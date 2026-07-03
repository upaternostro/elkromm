package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class UserEnablings extends ElkrommPacket {
    public UserEnablings(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.USER_ENABLINGS.getValue(), null);

        assert totalPackets == 0 : "UserEnablings: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "UserEnablings: index: expected 0, found " + index;
        assert dataLength == 0 : "UserEnablings: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "UserEnablings: data: mismatch in data length";
    }
}
