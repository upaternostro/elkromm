package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.PartitionArming} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Partitions</td><td>Bitmask of {@link org.paternostro.elkromm.ElkrommFacade.Partition}</td><td>{@link #PARTITIONS_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Arm status</td><td>Bitmask of armed partitions</td><td>{@link #ARM_STATUS_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PartitionArming implements ElkrommSerializer<org.paternostro.elkromm.dto.PartitionArming>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 2;

    /** Offset of the bitmask of partitions */
    public static final int PARTITIONS_OFFSET = 0x00;

    /** Offset of the bitmask of armed partitions */
    public static final int ARM_STATUS_OFFSET = 0x01;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PartitionArming obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[PARTITIONS_OFFSET] = obj.getPartitions();
        data[ARM_STATUS_OFFSET] = obj.getArmStatus();

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PartitionArming deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data length");
        
        return new org.paternostro.elkromm.dto.PartitionArming(data[PARTITIONS_OFFSET], data[ARM_STATUS_OFFSET]);
    }
}
