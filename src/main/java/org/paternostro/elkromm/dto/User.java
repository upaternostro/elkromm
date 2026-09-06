package org.paternostro.elkromm.dto;

/**
 * A user credential: a person authorized to arm/disarm partitions via a
 * keypad code, as opposed to a proximity {@link Key}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class User extends Credential
{
    /**
     * Creates a new user credential.
     *
     * @param ordinal 1-based position of this user (user 0 is TECNICO/installer, not representable here)
     * @param name display name, up to {@link org.paternostro.elkromm.ElkrommFacade#NAME_LENGTH} characters
     * @param enabling area/partition enabling flags for this user
     * @param associatedPartitions per-partition association flags
     */
    public User(int ordinal, String name, Enabling enabling, boolean[] associatedPartitions)
    {
        super(ordinal, name, enabling, associatedPartitions);
    }
}
