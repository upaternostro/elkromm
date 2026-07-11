package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

public class SingleKeyboard implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleKeyboard>
{
    public static final int KEYBOARD_SIZE   = 111;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleKeyboard obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                  data = new byte[length()];
        ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();

        data[0] = (byte)(obj.getIndex());
        System.arraycopy(kSerializer.serialize(obj.getKeyboard()), 0, data, 1, KEYBOARD_SIZE);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SingleKeyboard deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();
        byte[]                                                  kData = new byte[KEYBOARD_SIZE];

        System.arraycopy(data, 1, kData, 0, KEYBOARD_SIZE);
        org.paternostro.elkromm.dto.Keyboard keyboard = kSerializer.deserialize(kData);

        return new org.paternostro.elkromm.dto.SingleKeyboard(data[0], keyboard);
    }

    @Override
    public int length()
    {
        return KEYBOARD_SIZE + 1;
    }
}
