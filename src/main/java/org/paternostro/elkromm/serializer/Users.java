package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.User;

public class Users extends Credentials
{
    protected Credential allocateCredential(int ordinal, String name, Credential.Enabling enabling, boolean[] associatedPartitions)
    {
        return new User(ordinal, name, enabling, associatedPartitions);
    }
}
