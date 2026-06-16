package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

public class Partition implements Serializable, Comparable<Partition>
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
        setOrdinal(ordinal);
        setName(name);
        setVocalName(vocalName);
        setType(type);
        setEntryDelay(entryDelay);
        setExitDelay(exitDelay);
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(int ordinal)
    {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PARTITIONS) throw new IllegalArgumentException("Wrong ordinal " + ordinal + ", expected between 1 and " + ElkrommFacade.MAX_PARTITIONS);

        this.ordinal = ordinal;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

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
        if (type == null) throw new IllegalArgumentException("Missing mandatory type");
        if (type == Type.UNKNOWN) throw new IllegalArgumentException("Wrong type UNKNOWN");

        this.type = type;
    }

    public int getEntryDelay()
    {
        return entryDelay;
    }

    public void setEntryDelay(int entryDelay)
    {
        if (entryDelay < 0) throw new IllegalArgumentException("Wrong entry delay " + entryDelay);

        this.entryDelay = entryDelay;
    }

    public int getExitDelay()
    {
        return exitDelay;
    }

    public void setExitDelay(int exitDelay)
    {
        if (exitDelay < 0) throw new IllegalArgumentException("Wrong exit delay " + exitDelay);

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

    @Override
    public int compareTo(Partition o) {
        return Integer.compare(this.getOrdinal(), o.getOrdinal());
    }
}
