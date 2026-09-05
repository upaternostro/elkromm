package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SetUsers extends ElkrommPacket {
    public SetUsers(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_USERS.getValue(), data);

        assert totalPackets == 5 : "SetUsers: totalPackets: expected 5, found " + totalPackets;
        assert index >= 0 && index <= 5 : "SetUsers: index: expected 0...5, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "SetUsers: data: mismatch in data length";
    }
}
