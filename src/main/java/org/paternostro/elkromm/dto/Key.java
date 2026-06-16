package org.paternostro.elkromm.dto;

public class Key extends Credential
{
    public Key(int ordinal, String name, Enabling enabling, boolean[] associatedPartitions)
    {
        super(ordinal, name, enabling, associatedPartitions);
    }

    @Override
    public String toString()
    {
        return "Key{" +
                " ordinal=" + ordinal +
                " name='" + name + '\'' +
                '}';
    }
}
