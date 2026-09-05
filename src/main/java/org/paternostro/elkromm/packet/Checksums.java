package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Checksums extends ElkrommPacket {
    public Checksums(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, 0, 0, dataLength, ElkronCommand.CHECKSUM.getValue(), data);

        assert totalPackets == 0 : "Checksum: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Checksum: index: expected 0, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength == 52 && data != null && data.length == dataLength) : "Checksum: data: mismatch in data length";
    }
}
