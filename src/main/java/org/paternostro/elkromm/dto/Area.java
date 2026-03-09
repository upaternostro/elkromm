package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class Area implements Serializable
{
    private int         ordinal;
    private String      name;
    private boolean[]   associatedPartitions;

    public Area(int ordinal, String name, boolean[] associatedPartitions)
    {
        this.ordinal = ordinal;
        this.name = name;
        this.associatedPartitions = associatedPartitions;
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(int ordinal)
    {
        this.ordinal = ordinal;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
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

    @Override
    public String toString()
    {
        return "Area{" +
                "ordinal=" + ordinal +
                ", name='" + name + '\'' +
                '}';
    }
}
