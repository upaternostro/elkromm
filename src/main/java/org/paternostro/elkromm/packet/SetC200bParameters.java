package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SetC200bParameters extends ElkrommPacket {
    public SetC200bParameters(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_C200B.getValue(), data);

        assert totalPackets == 1 : "SetC200bParameters: totalPackets: expected 1, found " + totalPackets;
        assert index >= 0 && index <= 1 : "SetC200bParameters: index: expected 0...1, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "SetC200bParameters: data: mismatch in data length";
    }
}
