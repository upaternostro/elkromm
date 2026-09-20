package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * Abstract {@link org.paternostro.elkromm.dto.Credential} ({@link org.paternostro.elkromm.dto.User}/{@link org.paternostro.elkromm.dto.Key}) serializer, DTO &harr; byte array.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Key
 * @see User
 * @usedby {@link Credentials}
 * @usedby {@link SingleCredential}
 */
public abstract class Credential implements ElkrommSerializer<org.paternostro.elkromm.dto.Credential>
{
    public static final int PAYLOAD_SIZE = 26;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Credential obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

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
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        return allocateCredential(ElkrommUtils.getText(data, 2, ElkrommFacade.NAME_LENGTH), data[0], ElkrommUtils.unpackPartitions(data[1]));
    }

    protected org.paternostro.elkromm.dto.Credential allocateCredential(String name, byte enabling, boolean[] associatedPartitions)
    {
        throw new UnsupportedOperationException();
    }
}
