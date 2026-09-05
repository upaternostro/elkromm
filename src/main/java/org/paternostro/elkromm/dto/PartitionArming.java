package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade.Partition;

/**
 * Command to arm or disarm one or more partitions in a single call.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PartitionArming implements Serializable {
    private byte partitions;
    private byte armStatus;

    /**
     * Creates a new partition arming command.
     *
     * @param partitions bitmask selecting which partitions this command acts on
     * @param armStatus bitmask, same shape as {@code partitions}, with a bit set for each partition to arm
     */
    public PartitionArming(byte partitions, byte armStatus) {
        setPartitions(partitions);
        setArmStatus(armStatus);
    }

    /**
     * Returns the bitmask of partitions this command acts on.
     *
     * @return the partitions bitmask
     */
    public byte getPartitions() {
        return partitions;
    }

    /**
     * Sets the bitmask of partitions this command acts on.
     *
     * @param partitions a valid {@link Partition} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setPartitions(byte partitions) {
        if (!Partition.isValid(partitions)) throw new IllegalArgumentException("Wrong partitions value");

        this.partitions = partitions;
    }

    /**
     * Returns the arming bitmask.
     *
     * @return the arm status bitmask
     */
    public byte getArmStatus() {
        return armStatus;
    }

    /**
     * Sets the arming bitmask (a bit set for each partition to arm).
     *
     * @param armStatus a valid {@link Partition} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setArmStatus(byte armStatus) {
        if (!Partition.isValid(armStatus)) throw new IllegalArgumentException("Wrong arm status value");

        this.armStatus = armStatus;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{partitions=").append(partitions).append(", armStatus=").append(armStatus).append("}");

        return sb.toString();
    }
}
