package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.PartitionArming} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Partitions</td><td>Bitmask of {@link org.paternostro.elkromm.ElkrommFacade.Partition}</td></tr>
 *  <tr><td>1</td><td>Arm status</td><td>Bitmask of armed partitions</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PartitionArming implements ElkrommSerializer<org.paternostro.elkromm.dto.PartitionArming>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PartitionArming obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[0] = obj.getPartitions();
        data[1] = obj.getArmStatus();

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PartitionArming deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data length");
        
        return new org.paternostro.elkromm.dto.PartitionArming(data[0], data[1]);
    }

    @Override
    public int length() {
        return 2;
    }
}
