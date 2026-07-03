package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class Logout extends ElkrommPacket {
    public Logout(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.LOGOUT.getValue(), null);

        assert totalPackets == 0 : "Logout: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Logout: index: expected 0, found " + index;
        assert dataLength == 0 : "Logout: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "Logout: data: mismatch in data length";
    }
}
