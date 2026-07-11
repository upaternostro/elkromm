package org.paternostro.elkromm.serializer;

public class User extends Credential
{
    @Override
    protected org.paternostro.elkromm.dto.Credential allocateCredential(int ordinal, String name, byte enabling, boolean[] associatedPartitions)
    {
        return new org.paternostro.elkromm.dto.User(ordinal, name, org.paternostro.elkromm.dto.Credential.Enabling.valueOf(enabling), associatedPartitions);
    }
}
