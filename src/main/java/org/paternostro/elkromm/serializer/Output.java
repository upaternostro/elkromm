package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.Output} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Output's logical number</td><td>{@code 0x00} = unused slot</td><td>{@link #LOGICAL_NUMBER_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Type</td><td>{@link org.paternostro.elkromm.dto.Output.Type}: 0=unused, 1=normally low, 2=normally high</td><td>{@link #TYPE_OFFSET}</td></tr>
 *  <tr><td>0x02</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td><td>{@link #ASSOCIATED_PARTITIONS_OFFSET}</td></tr>
 *  <tr><td>0x03</td><td>Specialization</td><td>{@link org.paternostro.elkromm.dto.Output.Specialization}: 31 values (burglar, pre-alarm, tamper, gong, buzzer, partition status, ...)</td><td>{@link #SPECIALIZATION_OFFSET}</td></tr>
 *  <tr><td>0x04-0x07</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x08-0x1f</td><td>Name</td><td>24 bytes</td><td>{@link #NAME_OFFSET}</td></tr>
 *  <tr><td>0x20-0x24</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link Expansions}
 */
public class Output implements ElkrommSerializer<org.paternostro.elkromm.dto.Output>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 37;

    /** Offset of the output's logical number */
    public static final int LOGICAL_NUMBER_OFFSET        = 0x00;

    /** Offset of the type */
    public static final int TYPE_OFFSET                  = 0x01;

    /** Offset of the associated partitions */
    public static final int ASSOCIATED_PARTITIONS_OFFSET = 0x02;

    /** Offset of the specialization */
    public static final int SPECIALIZATION_OFFSET        = 0x03;

    /** Offset of the name */
    public static final int NAME_OFFSET                  = 0x08;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Output obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        if (obj.getLogicNumber() != 0) {
            data[LOGICAL_NUMBER_OFFSET] = (byte)(obj.getLogicNumber() & 0xFF);
            data[TYPE_OFFSET] = obj.getType().getValue();
            data[ASSOCIATED_PARTITIONS_OFFSET] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
            data[SPECIALIZATION_OFFSET] = obj.getSpecialization().getValue();
            ElkrommUtils.setText(data, NAME_OFFSET, obj.getName(), ElkrommFacade.NAME_LENGTH);
        }
        // else: unused output, skip

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Output deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        return data[LOGICAL_NUMBER_OFFSET] == 0 ? null : new org.paternostro.elkromm.dto.Output(data[LOGICAL_NUMBER_OFFSET], org.paternostro.elkromm.dto.Output.Type.valueOf(data[TYPE_OFFSET]), ElkrommUtils.unpackPartitions(data[ASSOCIATED_PARTITIONS_OFFSET]), org.paternostro.elkromm.dto.Output.Specialization.valueOf(data[SPECIALIZATION_OFFSET]), ElkrommUtils.getText(data, NAME_OFFSET, ElkrommFacade.NAME_LENGTH));
    }
}
