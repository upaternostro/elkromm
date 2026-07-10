package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Key;

public class Keys extends Credentials
{
    @Override
    protected Credential allocateCredential(int ordinal, String name, byte enabling, boolean[] associatedPartitions)
    {
        return new Key(ordinal, name, Credential.Enabling.valueOf((byte)(enabling & 0x01)), Key.Specialization.valueOf((byte)((enabling & 0x0C) >> 2)), associatedPartitions);
    }
}
