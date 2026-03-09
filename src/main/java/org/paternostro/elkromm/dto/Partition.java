package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class Partition implements Serializable
{
    public enum Type
    {
        STANDARD,
        SELF_EXCLUSION,
        ARMING_BLOCK,
        UNKNOWN         // should never happen...
    }

    private int         ordinal;
    private String      name;
    private boolean     vocalName;
    private Type        type;
    private int         entryDelay;
    private int         exitDelay;

    public Partition(int ordinal, String name, boolean vocalName, Type type, int entryDelay, int exitDelay)
    {
        this.ordinal = ordinal;
        this.name = name;
        this.vocalName = vocalName;
        this.type = type;
        this.entryDelay = entryDelay;
        this.exitDelay = exitDelay;
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

    public boolean isVocalName()
    {
        return vocalName;
    }

    public void setVocalName(boolean vocalName)
    {
        this.vocalName = vocalName;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public int getEntryDelay()
    {
        return entryDelay;
    }

    public void setEntryDelay(int entryDelay)
    {
        this.entryDelay = entryDelay;
    }

    public int getExitDelay()
    {
        return exitDelay;
    }

    public void setExitDelay(int exitDelay)
    {
        this.exitDelay = exitDelay;
    }

    @Override
    public String toString()
    {
        return "Partition{" +
                "ordinal=" + ordinal +
                ", name='" + name + '\'' +
                '}';
    }
}
