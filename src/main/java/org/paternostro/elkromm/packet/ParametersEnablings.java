package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ParametersEnablings extends ElkrommPacket {
    public ParametersEnablings(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.PARAMETERS_ENABLINGS.getValue(), data);

        assert totalPackets == 0 : "ParametersEnablings: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "ParametersEnablings: index: expected 0, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength == 30 && data != null && data.length == dataLength) : "ParametersEnablings: data: mismatch in data length";
    }
}
