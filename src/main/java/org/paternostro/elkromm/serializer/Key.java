package org.paternostro.elkromm.serializer;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Key extends Credential
{
    @Override
    protected org.paternostro.elkromm.dto.Credential allocateCredential(int ordinal, String name, byte enabling, boolean[] associatedPartitions)
    {
        return new org.paternostro.elkromm.dto.Key(ordinal, name, org.paternostro.elkromm.dto.Credential.Enabling.valueOf((byte)(enabling & 0x01)), org.paternostro.elkromm.dto.Key.Specialization.valueOf((byte)((enabling & 0x0C) >> 2)), associatedPartitions);
    }
}
