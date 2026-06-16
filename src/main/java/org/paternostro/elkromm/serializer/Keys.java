package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Key;

public class Keys extends Credentials
{
    protected Credential allocateCredential(int ordinal, String name, Credential.Enabling enabling, boolean[] associatedPartitions)
    {
        return new Key(ordinal, name, enabling, associatedPartitions);
    }
}
