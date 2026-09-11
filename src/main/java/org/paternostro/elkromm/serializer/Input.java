package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Input implements ElkrommSerializer<org.paternostro.elkromm.dto.Input>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Input obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[0] = (byte)(obj.getLogicNumber() & 0xFF);
        data[1] = obj.getConfiguration().getValue();
        data[2] = obj.getSpecialization().getValue();
        data[3] = (byte)(obj.getSensitivity().getValue() | obj.getFlags());
        data[4] = obj.getVideo().getValue();
        data[5] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        ElkrommUtils.setText(data, 6, obj.getName(), ElkrommFacade.NAME_LENGTH);
        data[34] = obj.getDelay().getValue();
        data[35] = data[36] = (byte)0xff;
        
        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Input deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        return data[0] == 0 ? null : new org.paternostro.elkromm.dto.Input(data[0], org.paternostro.elkromm.dto.Input.Configuration.valueOf(data[1]), org.paternostro.elkromm.dto.Input.Specialization.valueOf(data[2]), org.paternostro.elkromm.dto.Input.Sensitivity.valueOf((byte)(data[3] & ~org.paternostro.elkromm.dto.Input.Flags.IF_ALL.getValue())), (byte)(data[3] & org.paternostro.elkromm.dto.Input.Flags.IF_ALL.getValue()), org.paternostro.elkromm.dto.Input.Video.valueOf(data[4]), ElkrommUtils.unpackPartitions(data[5]), ElkrommUtils.getText(data, 6, ElkrommFacade.NAME_LENGTH), org.paternostro.elkromm.dto.Input.Delay.valueOf(data[34]));
    }

    @Override
    public int length()
    {
        return 38;
    }
}
