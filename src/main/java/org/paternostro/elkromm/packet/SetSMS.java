package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SetSMS extends ElkrommPacket {
    public SetSMS(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_SMS.getValue(), data);

        assert totalPackets == 2 && dataLength > 0 : "SetSMS: totalPackets: expected 2, found " + totalPackets;
        assert index >= 0 && index <= 2 : "SetSMS: index: expected 0...2, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "SetSMS: data: mismatch in data length";
    }
}
