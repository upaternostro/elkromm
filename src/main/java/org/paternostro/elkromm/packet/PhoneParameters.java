package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneParameters extends ElkrommPacket {
    public PhoneParameters(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.PHONE_PARAMETERS.getValue(), data);

        assert totalPackets == 0 : "PhoneParameters: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "PhoneParameters: index: expected 0, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "PhoneParameters: data: mismatch in data length";
    }
}
