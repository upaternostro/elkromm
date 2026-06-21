package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;

public class SystemStatus implements ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SystemStatus obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory parameter");

        byte[]  data = new byte[length()];

        data[0] = ElkrommUtils.packPartitions(obj.getActivePartitions());

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SystemStatus deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data length");

        org.paternostro.elkromm.dto.SystemStatus    retval = new org.paternostro.elkromm.dto.SystemStatus(ElkrommUtils.unpackPartitions(data[0]));

        return retval;
    }

    @Override
    public int length()
    {
        return 1;
    }
}
