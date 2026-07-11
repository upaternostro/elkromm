package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class UserProgramming extends ElkrommPacket {
    public UserProgramming(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.USER_PROGRAMMING.getValue(), data);

        assert totalPackets == 0 : "User: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "User: index: expected 0, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "User: data: mismatch in data length";
    }
}
