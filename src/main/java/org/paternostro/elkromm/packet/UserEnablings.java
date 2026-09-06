package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class UserEnablings extends ElkrommPacket {
    public UserEnablings(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.USER_ENABLINGS.getValue(), data);

        assert totalPackets == 0 : "UserEnablings: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "UserEnablings: index: expected 0, found " + index;
        assert dataLength == 0 || dataLength == 4: "UserEnablings: dataLength: expected 0 or 4, found " + dataLength;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength == 4 && data != null && data.length == dataLength) : "UserEnablings: data: mismatch in data length";
    }
}
