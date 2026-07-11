package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class KeypadProgramming extends ElkrommPacket {
    public KeypadProgramming(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.KEYPAD_PROGRAMMING.getValue(), data);

        assert totalPackets == 0 : "KeypadProgramming: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "KeypadProgramming: index: expected 0, found " + index;
        assert dataLength > 0 && data != null && data.length == dataLength : "KeypadProgramming: data: mismatch in data length";
    }
}
