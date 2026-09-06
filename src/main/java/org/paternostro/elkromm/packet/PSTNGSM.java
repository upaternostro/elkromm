package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PSTNGSM extends ElkrommPacket {
    public PSTNGSM(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.PSTN_GSM.getValue(), data);

        assert totalPackets == 0 : "PstnGsm: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "PstnGsm: index: expected 0, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "PstnGsm: data: mismatch in data length";
    }
}
