package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMS extends ElkrommPacket {
    public SMS(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SMS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets == 2 && dataLength > 0) : "SMS: totalPackets: expected 0 or 2, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && index <= 2 && dataLength > 0) : "SMS: index: expected 0...2, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "SMS: data: mismatch in data length";
    }
}
