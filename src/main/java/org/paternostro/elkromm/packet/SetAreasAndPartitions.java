package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class SetAreasAndPartitions extends ElkrommPacket {
    public SetAreasAndPartitions(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.SET_PARTITIONS_AND_AREAS.getValue(), data);

        assert totalPackets == 2 : "SetAreasAndPartitions: totalPackets: expected 2, found " + totalPackets;
        assert index >= 0 && index <= 2 : "SetAreasAndPartitions: index: expected 0, 1 or 2, found " + index;
        assert dataLength == 333 && data != null && data.length == dataLength : "SetAreasAndPartitions: data: mismatch in data length";
    }
}
