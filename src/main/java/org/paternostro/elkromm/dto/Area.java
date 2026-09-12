package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * An area: a named grouping of partitions, used to organize the panel's
 * arming logic above the partition level.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Area implements Serializable
{
    public static final Area    UNUSED = new Area("...                     ", ElkrommUtils.unpackPartitions((byte)0x01));

    private String      name;
    private boolean[]   associatedPartitions;

    /**
     * Creates a new area.
     *
     * @param ordinal 1-based position of this area, in range [1, {@link ElkrommFacade#MAX_AREAS}]
     * @param name display name
     * @param associatedPartitions per-partition association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public Area(String name, boolean[] associatedPartitions)
    {
        setName(name);
        setAssociatedPartitions(associatedPartitions);
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
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{name=").append(name).append(", associatedPartitions="
               ).append(Arrays.toString(associatedPartitions)).append("}");

        return sb.toString();
    }
}
