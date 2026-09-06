package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SetReaders extends ElkrommPacket {
    public SetReaders(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_READERS.getValue(), data);

        assert totalPackets >= 0 : "SetReaders: totalPackets: expected 0 or more, found " + totalPackets;
        assert index >= 0 : "SetReaders: index: expected 0 or more, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "SetReaders: data: mismatch in data length";
    }
}
