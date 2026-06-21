package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Output;

public class Outputs implements ElkrommSerializer<Output>
{
    @Override
    public byte[] serialize(Output obj)
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
    public Output deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        return data[0] == 0 ? null : new Output(data[0], Output.Type.valueOf(data[1]), ElkrommUtils.unpackPartitions(data[2]), Output.Specialization.valueOf(data[3]), ElkrommUtils.getText(data, 8, ElkrommFacade.NAME_LENGTH));
    }

    @Override
    public int length()
    {
        return 37;
    }
}
