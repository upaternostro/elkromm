package org.paternostro.elkromm.dto;

public class User extends Credential
{
    public User(int ordinal, String name, Enabling enabling, boolean[] associatedPartitions)
    {
        super(ordinal, name, enabling, associatedPartitions);
    }
}
