package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ExcludeIncludeInput extends ElkrommPacket {
    public ExcludeIncludeInput(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.EXCLUDE_INCLUDE_INPUT.getValue(), data);

        assert totalPackets == 0 : "ExcludeIncludeInput: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "ExcludeIncludeInput: index: expected 0, found " + index;
        assert dataLength == 2 : "ExcludeIncludeInput: dataLength: expected 2, found " + dataLength;
        assert data != null && data.length == dataLength : "ExcludeIncludeInput: data: mismatch in data length";
    }
}
