package org.paternostro.elkromm.serializer;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class User extends Credential
{
    @Override
    protected org.paternostro.elkromm.dto.Credential allocateCredential(String name, byte enabling, boolean[] associatedPartitions)
    {
        return new org.paternostro.elkromm.dto.User(name, org.paternostro.elkromm.dto.Credential.Enabling.valueOf(enabling), associatedPartitions);
    }
}
