package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * Abstract {@link Credential}s ({@link org.paternostro.elkromm.dto.User}s/{@link org.paternostro.elkromm.dto.Key}s) serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: {@link ElkrommFacade#MAX_CREDENTIALS} instances of {@link Credential}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see org.paternostro.elkromm.serializer.Credential
 */
public abstract class Credentials implements ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>
{
    public static final int PAYLOAD_SIZE = ElkrommFacade.MAX_CREDENTIALS*Credential.PAYLOAD_SIZE+ElkrommUtils.CHECKSUM_SIZE; // 32 utenti (ognuno con 2 byte di flag e 24 di nome) + 4 byte di checksum

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Credential[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                                                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   cSerializer = allocateSerializer();

        Arrays.fill(data, (byte)0x00);

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(cSerializer.serialize(obj[i]), 0, data, i * Credential.PAYLOAD_SIZE, Credential.PAYLOAD_SIZE);
        }
        
        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Credential[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        org.paternostro.elkromm.dto.Credential[]                    retval = new org.paternostro.elkromm.dto.Credential[ElkrommFacade.MAX_CREDENTIALS];
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   cSerializer = allocateSerializer();
        byte[]                                                      cData = new byte[Credential.PAYLOAD_SIZE];

        for (byte i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            System.arraycopy(data, i * Credential.PAYLOAD_SIZE, cData, 0, Credential.PAYLOAD_SIZE);
            retval[i] = cSerializer.deserialize(cData);
        }

        return retval;
    }

    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> allocateSerializer()
    {
        throw new UnsupportedOperationException("Unimplemented method 'allocateSerializer'");
    }
}
