package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Keyboard;

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
        int                         offset;       
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        for (int i = 0; i < obj.length; i++) {
            offset = i * KEYBOARD_SIZE;

            data[offset] = (byte)(obj[i].getAddress() & 0xFF);
            ElkrommUtils.setText(data, offset + 2, obj[i].getVersion(), 4);
            System.arraycopy(iSerializer.serialize(obj[i].getFirstInput()), 0, data, offset + 6, INPUT_SIZE);
            System.arraycopy(iSerializer.serialize(obj[i].getSecondInput()), 0, data, offset + 6 + INPUT_SIZE, INPUT_SIZE);
            data[offset + 6 + 2*INPUT_SIZE] = obj[i].getEnablings();
            data[offset + 7 + 2*INPUT_SIZE] = ElkrommUtils.packPartitions(obj[i].getAssociatedPartitions());
            data[offset + 8 + 2*INPUT_SIZE] = obj[i].getAudioFeatures();
            ElkrommUtils.setText(data, offset + 9 + 2*INPUT_SIZE, obj[i].getName(), ElkrommFacade.NAME_LENGTH);
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
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Keyboard[]                  retval = new Keyboard[(data.length - 4) / KEYBOARD_SIZE];
        int                         offset;
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[INPUT_SIZE];
        Input                       input1;
        Input                       input2;

        for (int i = 0; i < data.length / KEYBOARD_SIZE; i++) {
            offset = i * KEYBOARD_SIZE;

            System.arraycopy(data, offset + 6, iData, 0, INPUT_SIZE);
            input1 = iSerializer.deserialize(iData);

            System.arraycopy(data, offset + 6 + INPUT_SIZE, iData, 0, INPUT_SIZE);
            input2 = iSerializer.deserialize(iData);

            retval[i] = new Keyboard(data[offset], ElkrommUtils.getText(data, offset + 2, 4), input1, input2, data[offset + 6 + 2*INPUT_SIZE], ElkrommUtils.unpackPartitions(data[offset + 7 + 2*INPUT_SIZE]), data[offset + 8 + 2*INPUT_SIZE], ElkrommUtils.getText(data, offset + 9 + 2*INPUT_SIZE, ElkrommFacade.NAME_LENGTH));
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
