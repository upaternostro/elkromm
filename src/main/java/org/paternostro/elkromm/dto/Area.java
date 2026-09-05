package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * An area: a named grouping of partitions, used to organize the panel's
 * arming logic above the partition level.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Area implements Serializable, Comparable<Area>
{
    private int         ordinal;
    private String      name;
    private boolean[]   associatedPartitions;

    /**
     * Creates a new area.
     *
     * @param ordinal 1-based position of this area, in range [1, {@link ElkrommFacade#MAX_AREAS}]
     * @param name display name
     * @param associatedPartitions per-partition association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public Area(int ordinal, String name, boolean[] associatedPartitions)
    {
        setOrdinal(ordinal);
        setName(name);
        setAssociatedPartitions(associatedPartitions);
    }

    /**
     * Returns the 1-based ordinal of this area.
     *
     * @return the ordinal
     */
    public int getOrdinal()
    {
        return ordinal;
    }

    /**
     * Sets the 1-based ordinal of this area.
     *
     * @param ordinal the ordinal to set, in range [1, {@link ElkrommFacade#MAX_AREAS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setOrdinal(int ordinal)
    {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_AREAS) throw new IllegalArgumentException("Wrong ordinal " + ordinal + ", expected between 1 and " + ElkrommFacade.MAX_AREAS);

        this.ordinal = ordinal;
    }

    /**
     * Returns the display name of this area.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this area.
     *
     * @param name the name to set, not {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

        this.name = name;
    }

    /**
     * Returns a copy of this area's per-partition association flags.
     *
     * @return the association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Sets this area's per-partition association flags.
     *
     * @param associatedPartitions the flags to set, not {@code null}
     * @throws IllegalArgumentException if {@code associatedPartitions} is {@code null}
     */
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
