package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class AreasAndPartitions extends ElkrommPacket {
    public AreasAndPartitions(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(dataLength == 0 ? Direction.FROM_CLIENT : Direction.TO_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.PARTITIONS_AND_AREAS.getValue(), data);

        assert (totalPackets == 0 && dataLength == 0) || (totalPackets == 2 && dataLength > 0) : "PartitionsAndAreas: totalPackets: expected 0 or 2, found " + totalPackets;
        assert (index == 0 && dataLength == 0) || (index >= 0 && index <= 2 && dataLength > 0) : "PartitionsAndAreas: index: expected 0, 1 or 2, found " + index;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "PartitionsAndAreas: data: mismatch in data length";
    }
}
