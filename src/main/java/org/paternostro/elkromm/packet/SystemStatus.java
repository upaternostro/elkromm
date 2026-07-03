package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class SystemStatus extends ElkrommPacket {
    public SystemStatus(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, 0, 0, dataLength, ElkronCommand.SYSTEM_STATUS.getValue(), data);

        assert totalPackets == 0 : "SystemStatus: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "SystemStatus: index: expected 0, found " + index;
        assert dataLength == 0 || dataLength == 1: "SystemStatus: dataLength: expected 0 or 1, found " + dataLength;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength == 1 && data != null && data.length == 1) : "SystemStatus: data: mismatch in data length";
    }
}
