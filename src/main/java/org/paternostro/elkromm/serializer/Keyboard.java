package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Keyboard implements ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard>
{
    public static final int KEYBOARD_SIZE   = 111;
    public static final int INPUT_SIZE      = 38;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Keyboard obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                      data = new byte[KEYBOARD_SIZE];
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        data[0] = (byte)(obj.getAddress() & 0xFF);
        ElkrommUtils.setText(data, 2, obj.getVersion(), 4);
        System.arraycopy(iSerializer.serialize(obj.getFirstInput()), 0, data, 6, INPUT_SIZE);
        System.arraycopy(iSerializer.serialize(obj.getSecondInput()), 0, data, 6 + INPUT_SIZE, INPUT_SIZE);
        data[6 + 2*INPUT_SIZE] = obj.getEnablings();
        data[7 + 2*INPUT_SIZE] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        data[8 + 2*INPUT_SIZE] = obj.getAudioFeatures();
        ElkrommUtils.setText(data, 9 + 2*INPUT_SIZE, obj.getName(), ElkrommFacade.NAME_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Keyboard deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[INPUT_SIZE];
        Input                       input1;
        Input                       input2;

        System.arraycopy(data, 6, iData, 0, INPUT_SIZE);
        input1 = iSerializer.deserialize(iData);

        System.arraycopy(data, 6 + INPUT_SIZE, iData, 0, INPUT_SIZE);
        input2 = iSerializer.deserialize(iData);

        return new org.paternostro.elkromm.dto.Keyboard(data[0], ElkrommUtils.getText(data, 2, 4), input1, input2, data[6 + 2*INPUT_SIZE], ElkrommUtils.unpackPartitions(data[7 + 2*INPUT_SIZE]), data[8 + 2*INPUT_SIZE], ElkrommUtils.getText(data, 9 + 2*INPUT_SIZE, ElkrommFacade.NAME_LENGTH));
    }

    @Override
    public int length()
    {
        return KEYBOARD_SIZE;
    }
}
