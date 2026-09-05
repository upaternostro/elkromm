package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Hello extends ElkrommPacket {
    public Hello(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.HELLO.getValue(), null);

        assert totalPackets == 0 : "Hello: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Hello: index: expected 0, found " + index;
        assert dataLength == 0 : "Hello: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "Hello: data: mismatch in data length";
    }

    public Hello(int plantCode12, int plantCode34) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.HELLO.getValue(), null);
    }
}
