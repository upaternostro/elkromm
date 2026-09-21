package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

/**
 * {@link org.paternostro.elkromm.dto.SingleKeyboard} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Index</td><td>Keyboard index</td><td>{@link #INDEX_OFFSET}</td></tr>
 *  <tr><td>0x01-0x6f</td><td>Keyboard</td><td>See {@link Keyboard}</td><td>{@link #KEYBOARD_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Keyboard
 */
public class SingleKeyboard implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleKeyboard>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = Keyboard.PAYLOAD_SIZE + 1;

    /** Offset of the index */
    public static final int INDEX_OFFSET    = 0x00;

    /** Offset of the keyboard, see {@link Keyboard} */
    public static final int KEYBOARD_OFFSET = INDEX_OFFSET + 1;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleKeyboard obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                  data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();

        data[INDEX_OFFSET] = (byte)(obj.getIndex());
        System.arraycopy(kSerializer.serialize(obj.getKeyboard()), 0, data, KEYBOARD_OFFSET, Keyboard.PAYLOAD_SIZE);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SingleKeyboard deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();
        byte[]                                                  kData = new byte[Keyboard.PAYLOAD_SIZE];

        System.arraycopy(data, KEYBOARD_OFFSET, kData, 0, Keyboard.PAYLOAD_SIZE);
        org.paternostro.elkromm.dto.Keyboard keyboard = kSerializer.deserialize(kData);

        return new org.paternostro.elkromm.dto.SingleKeyboard(data[INDEX_OFFSET], keyboard);
    }
}
