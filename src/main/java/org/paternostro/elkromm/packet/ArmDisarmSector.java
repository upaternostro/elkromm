package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkronCommand;

public class ArmDisarmSector extends ElkrommPacket {
    public ArmDisarmSector(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 0, ElkronCommand.ARM_DISARM_SECTOR.getValue(), null);

        assert totalPackets == 0 : "ArmDisarmSector: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "ArmDisarmSector: index: expected 0, found " + index;
        assert dataLength == 0 : "ArmDisarmSector: dataLength: expected 0, found " + dataLength;
        assert dataLength == 0 && (data == null || data.length == 0) : "ArmDisarmSector: data: mismatch in data length";
    }
}
