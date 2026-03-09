package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class Key implements Serializable
{
    private boolean[]   associatedPartitions;
    private String      name;

    public Key(boolean[] associatedPartitions, String name)
    {
        this.associatedPartitions = associatedPartitions;
        this.name = name;
    }

    public boolean[] getAssociatedPartitions()
    {
        return associatedPartitions;
    }

    public void setAssociatedPartitions(boolean[] associatedPartitions)
    {
        this.associatedPartitions = associatedPartitions;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Key{" +
                "name='" + name + '\'' +
                '}';
    }
}
