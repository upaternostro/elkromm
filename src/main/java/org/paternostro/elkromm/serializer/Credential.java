package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public abstract class Credential implements ElkrommSerializer<org.paternostro.elkromm.dto.Credential>
{
    public static final int CREDENTIAL_SIZE = 1+1+ElkrommFacade.NAME_LENGTH;  // 2 byte di flag e 24 di nome

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Credential obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[0] = obj.getEnablingValue();
        data[1] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        ElkrommUtils.setText(data, 2, obj.getName(), ElkrommFacade.NAME_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Credential deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        return allocateCredential(1, ElkrommUtils.getText(data, 2, ElkrommFacade.NAME_LENGTH), data[0], ElkrommUtils.unpackPartitions(data[1]));
    }

    protected org.paternostro.elkromm.dto.Credential allocateCredential(int ordinal, String name, byte enabling, boolean[] associatedPartitions)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int length()
    {
        return CREDENTIAL_SIZE;
    }
}
