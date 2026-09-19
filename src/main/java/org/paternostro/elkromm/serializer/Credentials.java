package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential;

/**
 * Abstract {@link Credential}s ({@link org.paternostro.elkromm.dto.User}s/{@link org.paternostro.elkromm.dto.Key}s) serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: {@link ElkrommFacade#MAX_CREDENTIALS} instances of {@link Credential}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see org.paternostro.elkromm.serializer.Credential
 */
public abstract class Credentials implements ElkrommSerializer<Credential[]>
{
    @Override
    public byte[] serialize(Credential[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                          data = new byte[length()];
        ElkrommSerializer<Credential>   cSerializer = allocateSerializer();

        Arrays.fill(data, (byte)0x00);

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(cSerializer.serialize(obj[i]), 0, data, i * SerializersConstants.CREDENTIAL_SIZE, SerializersConstants.CREDENTIAL_SIZE);
        }
        
        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public Credential[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Credential[]                    retval = new Credential[ElkrommFacade.MAX_CREDENTIALS];
        ElkrommSerializer<Credential>   cSerializer = allocateSerializer();
        byte[]                          cData = new byte[SerializersConstants.CREDENTIAL_SIZE];

        for (byte i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            System.arraycopy(data, i * SerializersConstants.CREDENTIAL_SIZE, cData, 0, SerializersConstants.CREDENTIAL_SIZE);
            retval[i] = cSerializer.deserialize(cData);
        }

        return retval;
    }

    protected ElkrommSerializer<Credential> allocateSerializer()
    {
        throw new UnsupportedOperationException("Unimplemented method 'allocateSerializer'");
    }

    @Override
    public int length()
    {
        return ElkrommFacade.MAX_CREDENTIALS*(SerializersConstants.CREDENTIAL_SIZE)+4; // 32 utenti (ognuno con 2 byte di flag e 24 di nome) + 4 byte di checksum
    }
}
