package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input;

public class Reader implements ElkrommSerializer<org.paternostro.elkromm.dto.Reader>
{
    public static final int INPUT_SIZE  = 38;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Reader obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                      data = new byte[length()];
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        data[0] = (byte)(obj.getAddress() & 0xFF);
        System.arraycopy(iSerializer.serialize(obj.getFirstInput()), 0, data, 6, INPUT_SIZE);
        System.arraycopy(iSerializer.serialize(obj.getSecondInput()), 0, data, 6 + INPUT_SIZE, INPUT_SIZE);
        data[6 + 2*INPUT_SIZE] = obj.getLed1() == null ? 0 : obj.getLed1().getBitMask();
        data[7 + 2*INPUT_SIZE] = obj.getLed2() == null ? 0 : obj.getLed2().getBitMask();
        data[8 + 2*INPUT_SIZE] = obj.getLed3() == null ? 0 : obj.getLed3().getBitMask();
        data[9 + 2*INPUT_SIZE] = obj.getLed4() == null ? 0 : obj.getLed4().getBitMask();
        data[10 + 2*INPUT_SIZE] = obj.getEnablings();
        ElkrommUtils.setText(data, 11 + 2*INPUT_SIZE, obj.getName(), ElkrommFacade.NAME_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Reader deserialize(byte[] data)
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

        return new org.paternostro.elkromm.dto.Reader(data[0], input1, input2, ElkrommFacade.Partition.valueOf(data[6 + 2*INPUT_SIZE]), ElkrommFacade.Partition.valueOf(data[7 + 2*INPUT_SIZE]), ElkrommFacade.Partition.valueOf(data[8 + 2*INPUT_SIZE]), ElkrommFacade.Partition.valueOf(data[9 + 2*INPUT_SIZE]), data[10 + 2*INPUT_SIZE], ElkrommUtils.getText(data, 11 + 2*INPUT_SIZE, ElkrommFacade.NAME_LENGTH));
    }

    @Override
    public int length()
    {
        return 113;
    }
}
