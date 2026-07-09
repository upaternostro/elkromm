package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class SMSProgramming extends ElkrommPacket {
    public SMSProgramming(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SMS_PROGRAMMING.getValue(), data);

        assert totalPackets == 0 : "SMSProgramming: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "SMSProgramming: index: expected 0, found " + index;
        assert dataLength == 41 && data != null && data.length == dataLength : "SMSProgramming: data: mismatch in data length";
    }
}
