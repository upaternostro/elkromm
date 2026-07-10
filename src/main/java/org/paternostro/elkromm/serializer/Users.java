package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.User;

public class Users extends Credentials
{
    @Override
    protected Credential allocateCredential(int ordinal, String name, byte enabling, boolean[] associatedPartitions)
    {
        return new User(ordinal, name, Credential.Enabling.valueOf(enabling), associatedPartitions);
    }
}
