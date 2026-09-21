package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.SystemStatus} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Active partitions</td><td>Bitmask, LSB = partition 1</td><td>{@link #ACTIVE_PARTITIONS_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SystemStatus implements ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 1;

    /** Offset of the bitmask of active partitions */
    public static final int ACTIVE_PARTITIONS_OFFSET = 0x00;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SystemStatus obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory parameter");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[ACTIVE_PARTITIONS_OFFSET] = ElkrommUtils.packPartitions(obj.getActivePartitions());

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SystemStatus deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data length");

        org.paternostro.elkromm.dto.SystemStatus    retval = new org.paternostro.elkromm.dto.SystemStatus(ElkrommUtils.unpackPartitions(data[ACTIVE_PARTITIONS_OFFSET]));

        return retval;
    }
}
