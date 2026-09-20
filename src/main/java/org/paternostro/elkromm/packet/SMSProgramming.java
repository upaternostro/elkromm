package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;
import org.paternostro.elkromm.serializer.SingleSMS;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMSProgramming extends ElkrommPacket {
    public SMSProgramming(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SMS_PROGRAMMING.getValue(), data);

        assert totalPackets == 0 : "SMSProgramming: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "SMSProgramming: index: expected 0, found " + index;
        assert dataLength == SingleSMS.PAYLOAD_SIZE && data != null && data.length == dataLength : "SMSProgramming: data: mismatch in data length";
    }
}
