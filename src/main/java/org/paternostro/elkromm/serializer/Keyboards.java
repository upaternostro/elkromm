package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Keyboard;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Keyboards implements ElkrommSerializer<Keyboard[]>
{
    public static final int KEYBOARD_SIZE   = 111;
    public static final int INPUT_SIZE      = 38;

    @Override
    public byte[] serialize(Keyboard[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * KEYBOARD_SIZE + 4];
        ElkrommSerializer<Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();

        Arrays.fill(data, (byte)0x00);

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(kSerializer.serialize(obj[i]), 0, data, i * KEYBOARD_SIZE, KEYBOARD_SIZE);
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public Keyboard[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % KEYBOARD_SIZE != 4) throw new IllegalArgumentException("Wrong data size");

        int patchOffset = 0;
        while ((patchOffset += KEYBOARD_SIZE - 2) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - 4) {
            for (int j = 0; j < 2; j++) {
                data[patchOffset + 6 + j * INPUT_SIZE + 3] &= ~0x10;
            }
            patchOffset += KEYBOARD_SIZE;
        }

        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Keyboard[]                  retval = new Keyboard[(data.length - 4) / KEYBOARD_SIZE];
        ElkrommSerializer<Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();
        byte[]                      kData = new byte[KEYBOARD_SIZE];

        for (int i = 0; i < data.length / KEYBOARD_SIZE; i++) {
            System.arraycopy(data, i * KEYBOARD_SIZE, kData, 0, KEYBOARD_SIZE);
            retval[i] = kSerializer.deserialize(kData);
        }

        return retval;
    }

    @Override
    public int length()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'length'");
    }
}
