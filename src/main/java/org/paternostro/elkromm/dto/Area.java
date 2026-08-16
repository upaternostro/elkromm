package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class Area implements Serializable, Comparable<Area>
{
    private int         ordinal;
    private String      name;
    private boolean[]   associatedPartitions;

    public Area(int ordinal, String name, boolean[] associatedPartitions)
    {
        setOrdinal(ordinal);
        setName(name);
        setAssociatedPartitions(associatedPartitions);
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(int ordinal)
    {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_AREAS) throw new IllegalArgumentException("Wrong ordinal " + ordinal + ", expected between 1 and " + ElkrommFacade.MAX_AREAS);

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

    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public void setAssociatedPartitions(boolean[] associatedPartitions)
    {
        if (associatedPartitions == null) throw new IllegalArgumentException("Missing mandatory associated partitions");

        this.associatedPartitions = Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{ordinal=").append(ordinal).append(", name=").append(name).append(", associatedPartitions="
               ).append(Arrays.toString(associatedPartitions)).append("}");

        return sb.toString();
    }

    @Override
    public int compareTo(Area o) {
        return Integer.compare(this.getOrdinal(), o.getOrdinal());
    }
}
