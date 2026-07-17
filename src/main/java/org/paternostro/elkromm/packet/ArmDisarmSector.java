package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class ArmDisarmSector extends ElkrommPacket {
    public ArmDisarmSector(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.ARM_DISARM_SECTOR.getValue(), data);

        assert totalPackets == 0 : "ArmDisarmSector: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "ArmDisarmSector: index: expected 0, found " + index;
        assert dataLength == 2 : "ArmDisarmSector: dataLength: expected 2, found " + dataLength;
        assert dataLength == 2 && (data == null || data.length == 2) : "ArmDisarmSector: data: mismatch in data length";
    }
}
