package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade.Partition;

public class PartitionArming implements Serializable {
    private byte partitions;
    private byte armStatus;

    public PartitionArming(byte partitions, byte armStatus) {
        setPartitions(partitions);
        setArmStatus(armStatus);
    }

    public byte getPartitions() {
        return partitions;
    }

    public void setPartitions(byte partitions) {
        if (!Partition.isValid(partitions)) throw new IllegalArgumentException("Wrong partitions value");

        this.partitions = partitions;
    }

    public byte getArmStatus() {
        return armStatus;
    }

    public void setArmStatus(byte armStatus) {
        if (!Partition.isValid(armStatus)) throw new IllegalArgumentException("Wrong arm status value");

        this.armStatus = armStatus;
    }
}
