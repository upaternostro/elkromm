package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.Output} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><td>Offset</td><td>Meaning</td><td>Note</td></tr>
 *  <tr><td>0</td><td>Output's logical number</td><td>{@code 0x00} = unused slot</td></tr>
 *  <tr><td>1</td><td>Type</td><td>{@link org.paternostro.elkromm.dto.Output.Type}: 0=unused, 1=normally low, 2=normally high</td></tr>
 *  <tr><td>2</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td></tr>
 *  <tr><td>3</td><td>Specialization</td><td>{@link org.paternostro.elkromm.dto.Output.Specialization}: 31 values (burglar, pre-alarm, tamper, gong, buzzer, partition status, ...)</td></tr>
 *  <tr><td>4-7</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>8-31</td><td>Name</td><td>24 bytes</td></tr>
 *  <tr><td>32-36</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link Expansions}
 */
public class Output implements ElkrommSerializer<org.paternostro.elkromm.dto.Output>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Output obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        if (obj.getLogicNumber() != 0) {
            data[0] = (byte)(obj.getLogicNumber() & 0xFF);
            data[1] = obj.getType().getValue();
            data[2] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
            data[3] = obj.getSpecialization().getValue();
            ElkrommUtils.setText(data, 8, obj.getName(), ElkrommFacade.NAME_LENGTH);
        }
        // else: unused output, skip

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Output deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        return data[0] == 0 ? null : new org.paternostro.elkromm.dto.Output(data[0], org.paternostro.elkromm.dto.Output.Type.valueOf(data[1]), ElkrommUtils.unpackPartitions(data[2]), org.paternostro.elkromm.dto.Output.Specialization.valueOf(data[3]), ElkrommUtils.getText(data, 8, ElkrommFacade.NAME_LENGTH));
    }

    @Override
    public int length()
    {
        return SerializersConstants.OUTPUT_SIZE;
    }
}
