package org.paternostro.elkromm.serializer;

import java.util.List;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.Login} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-3</td><td>Plant code</td><td>BCD coded</td></tr>
 *  <tr><td>4-6</td><td>Technical PIN code</td><td>BCD coded</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Login implements ElkrommSerializer<org.paternostro.elkromm.dto.Login>
{
    public static final int PAYLOAD_SIZE = 7;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Login obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        List<Byte>  plantCodeBytes = ElkrommUtils.bcd(obj.getPlantCode(), 4);
        List<Byte>  technicalCodeBytes = ElkrommUtils.bcd(obj.getTechnicalCode(), 3);

        plantCodeBytes.addAll(technicalCodeBytes);

        return ElkrommUtils.listToArray(plantCodeBytes);
    }

    @Override
    public org.paternostro.elkromm.dto.Login deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        return new org.paternostro.elkromm.dto.Login(
            ElkrommUtils.dcbByte(data[0]) * 1000000 + ElkrommUtils.dcbByte(data[1]) * 10000 + ElkrommUtils.dcbByte(data[2]) * 100 + ElkrommUtils.dcbByte(data[3]),
            ElkrommUtils.dcbByte(data[4]) * 10000 + ElkrommUtils.dcbByte(data[5]) * 100 + ElkrommUtils.dcbByte(data[6])
        );
    }
}
