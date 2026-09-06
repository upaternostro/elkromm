package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Users extends ElkrommPacket {
    public Users(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.USERS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets == 5 && dataLength > 0) : "Users: totalPackets: expected 0 or 5, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && index <= 5 && dataLength > 0) : "Users: index: expected 0...5, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "Users: data: mismatch in data length";
    }
}
