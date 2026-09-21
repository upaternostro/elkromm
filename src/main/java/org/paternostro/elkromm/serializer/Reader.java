package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input;

/**
 * {@link org.paternostro.elkromm.dto.Reader} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Bus address</td><td>Also identifies the reader in single-instance writes (there's no separate {@code SingleReader} wrapper)</td><td>{@link #BUS_ADDRESS_OFFSET}</td></tr>
 *  <tr><td>0x01-0x05</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x06-0x2b</td><td>First onboard input</td><td>38 bytes, see {@link org.paternostro.elkromm.serializer.Input}</td><td>{@link #FIRST_INPUT_OFFSET}</td></tr>
 *  <tr><td>0x2c-0x51</td><td>Second onboard input</td><td>38 bytes, see {@link org.paternostro.elkromm.serializer.Input}</td><td>{@link #SECOND_INPUT_OFFSET}</td></tr>
 *  <tr><td>0x52</td><td>LED 1</td><td>Associated partition ({@link ElkrommFacade.Partition}), {@code 0x00} = unused</td><td>{@link #LED_1_OFFSET}</td></tr>
 *  <tr><td>0x53</td><td>LED 2</td><td>Associated partition</td><td>{@link #LED_2_OFFSET}</td></tr>
 *  <tr><td>0x54</td><td>LED 3</td><td>Associated partition</td><td>{@link #LED_3_OFFSET}</td></tr>
 *  <tr><td>0x55</td><td>LED 4</td><td>Associated partition</td><td>{@link #LED_4_OFFSET}</td></tr>
 *  <tr><td>0x56</td><td>Enablings bitmask</td><td>{@link org.paternostro.elkromm.dto.Reader.Enablings}: only {@code MASKING} (0x01) known</td><td>{@link #ENABLINGS_OFFSET}</td></tr>
 *  <tr><td>0x57-0x6e</td><td>Name</td><td>24 bytes</td><td>{@link #NAME_OFFSET}</td></tr>
 *  <tr><td>0x6f-0x70</td><td>?</td><td>Not mapped by any DTO field. Zeroed on write by {@link Readers} in analogy with {@link Expansions}/{@link Keyboards}, <b>unverified on real hardware</b> (the author owns no physical readers) — for this reason the block checksum remains a warning, not an exception, on this structure</td><td>{@link #PSEUDO_CHECKSUM_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Input
 * @usedby {@link Readers}
 */
public class Reader implements ElkrommSerializer<org.paternostro.elkromm.dto.Reader>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 113;

    /** Offset of the bus address */
    public static final int BUS_ADDRESS_OFFSET     = 0x00;

    /** Offset of the first onboard input */
    public static final int FIRST_INPUT_OFFSET     = 0x06;

    /** Offset of the second onboard input */
    public static final int SECOND_INPUT_OFFSET    = FIRST_INPUT_OFFSET + org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE;

    /** Offset of the partition associated to LED 1 */
    public static final int LED_1_OFFSET           = SECOND_INPUT_OFFSET + org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE;

    /** Offset of the partition associated to LED 2 */
    public static final int LED_2_OFFSET           = LED_1_OFFSET + 1;

    /** Offset of the partition associated to LED 3 */
    public static final int LED_3_OFFSET           = LED_2_OFFSET + 1;

    /** Offset of the partition associated to LED 4 */
    public static final int LED_4_OFFSET           = LED_3_OFFSET + 1;

    /** Offset of the enablings bitmask */
    public static final int ENABLINGS_OFFSET       = LED_4_OFFSET + 1;

    /** Offset of the reader name */
    public static final int NAME_OFFSET            = ENABLINGS_OFFSET + 1;

    /** Offset of the pseudo checksum, excluded from the block checksum calculation */
    public static final int PSEUDO_CHECKSUM_OFFSET = NAME_OFFSET + ElkrommFacade.NAME_LENGTH;

    /** Size of the pseudo checksum */
    public static final int PSEUDO_CHECKSUM_SIZE   = 2;

    /** Number of onboard inputs */
    public static final int ONBOARD_INPUTS         = 2;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Reader obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        data[BUS_ADDRESS_OFFSET] = (byte)(obj.getAddress() & 0xFF);
        System.arraycopy(iSerializer.serialize(obj.getFirstInput()), 0, data, FIRST_INPUT_OFFSET, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        System.arraycopy(iSerializer.serialize(obj.getSecondInput()), 0, data, SECOND_INPUT_OFFSET, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        data[LED_1_OFFSET] = obj.getLed1() == null ? 0 : obj.getLed1().getValue();
        data[LED_2_OFFSET] = obj.getLed2() == null ? 0 : obj.getLed2().getValue();
        data[LED_3_OFFSET] = obj.getLed3() == null ? 0 : obj.getLed3().getValue();
        data[LED_4_OFFSET] = obj.getLed4() == null ? 0 : obj.getLed4().getValue();
        data[ENABLINGS_OFFSET] = obj.getEnablings();
        ElkrommUtils.setText(data, NAME_OFFSET, obj.getName(), ElkrommFacade.NAME_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Reader deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE];
        Input                       input1;
        Input                       input2;

        System.arraycopy(data, FIRST_INPUT_OFFSET, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        input1 = iSerializer.deserialize(iData);

        System.arraycopy(data, SECOND_INPUT_OFFSET, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        input2 = iSerializer.deserialize(iData);

        return new org.paternostro.elkromm.dto.Reader(data[BUS_ADDRESS_OFFSET], input1, input2, ElkrommFacade.Partition.valueOf(data[LED_1_OFFSET]), ElkrommFacade.Partition.valueOf(data[LED_2_OFFSET]), ElkrommFacade.Partition.valueOf(data[LED_3_OFFSET]), ElkrommFacade.Partition.valueOf(data[LED_4_OFFSET]), data[ENABLINGS_OFFSET], ElkrommUtils.getText(data, NAME_OFFSET, ElkrommFacade.NAME_LENGTH));
    }
}
