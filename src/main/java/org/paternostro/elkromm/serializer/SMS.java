package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMS implements ElkrommSerializer<org.paternostro.elkromm.dto.SMS>
{
    public static final int INPUT_SIZE  = 38;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SMS obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        Arrays.fill(data, 0, data.length, (byte)0xff); // Pad with 0xff bytes
        ElkrommUtils.setText(data, 0, obj.getText(), obj.getText().length());

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SMS deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        StringBuffer    sb = new StringBuffer();
        byte[]          tempArray = new byte[1];

        for (byte pivot : data) {
            if (pivot == (byte)0xff) break;

            tempArray[0] = pivot;
            sb.append(new String(tempArray));
        }

        return new org.paternostro.elkromm.dto.SMS(sb.toString());
    }

    @Override
    public int length()
    {
        return 40;
    }
}
