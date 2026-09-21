package org.paternostro.elkromm.serializer;

import java.util.List;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.Login} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x03</td><td>Plant code</td><td>BCD coded</td><td>{@link #PLANT_CODE_OFFSET}</td></tr>
 *  <tr><td>0x04-0x06</td><td>Technical PIN code</td><td>BCD coded</td><td>{@link #TECHNICAL_PIN_CODE_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Login implements ElkrommSerializer<org.paternostro.elkromm.dto.Login>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 7;

    /** Offset of the plant code, BCD coded */
    public static final int PLANT_CODE_OFFSET         = 0x00;

    /** Offset of the technical PIN code, BCD coded */
    public static final int TECHNICAL_PIN_CODE_OFFSET = 0x04;

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
            ElkrommUtils.dcbByte(data[PLANT_CODE_OFFSET]) * 1000000 + ElkrommUtils.dcbByte(data[PLANT_CODE_OFFSET + 1]) * 10000 + ElkrommUtils.dcbByte(data[PLANT_CODE_OFFSET + 2]) * 100 + ElkrommUtils.dcbByte(data[PLANT_CODE_OFFSET + 3]),
            ElkrommUtils.dcbByte(data[TECHNICAL_PIN_CODE_OFFSET]) * 10000 + ElkrommUtils.dcbByte(data[TECHNICAL_PIN_CODE_OFFSET + 1]) * 100 + ElkrommUtils.dcbByte(data[TECHNICAL_PIN_CODE_OFFSET + 2])
        );
    }
}
