package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Send extends ElkrommPacket {
    public Send(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.SEND.getValue(), null);

        assert totalPackets == 0 : "Send: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Send: index: expected 0, found " + index;
        assert dataLength == 0 : "Send: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "Send: data: mismatch in data length";
    }

    public Send(int plantCode12, int plantCode34) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.SEND.getValue(), null);
    }
}
