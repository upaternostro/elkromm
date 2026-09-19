package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.SystemStatus} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Active partitions</td><td>Bitmask, LSB = partition 1</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SystemStatus implements ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SystemStatus obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory parameter");

        byte[]  data = new byte[length()];

        data[0] = ElkrommUtils.packPartitions(obj.getActivePartitions());

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SystemStatus deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data length");

        org.paternostro.elkromm.dto.SystemStatus    retval = new org.paternostro.elkromm.dto.SystemStatus(ElkrommUtils.unpackPartitions(data[0]));

        return retval;
    }

    @Override
    public int length()
    {
        return 1;
    }
}
