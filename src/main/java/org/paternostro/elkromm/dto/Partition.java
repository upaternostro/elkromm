package org.paternostro.elkromm.dto;

import java.io.Serializable;

/**
 * A partition (a.k.a. sector): the smallest independently armable unit of
 * the panel, with its own entry/exit delays.
 * <p>
 * Not to be confused with {@link org.paternostro.elkromm.ElkrommFacade.Partition},
 * the bitmask enum used elsewhere in the API to select one or more partitions.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Partition implements Serializable
{
    public static final Partition   UNUSED = new Partition("...                     ", false, Type.STANDARD, 0, 0);

    /** How a partition behaves with respect to self-exclusion/arming restrictions. */
    public enum Type
    {
        /** Ordinary partition, no special behavior. */
        STANDARD,
        /** The partition can exclude itself. */
        SELF_EXCLUSION,
        /** Arming this partition is blocked under some condition. */
        ARMING_BLOCK,
        /** Sentinel for a raw value with no known meaning; never a valid value to set. */
        UNKNOWN         // should never happen...
    }

    private String      name;
    private boolean     vocalName;
    private Type        type;
    private int         entryDelay;
    private int         exitDelay;

    /**
     * Creates a new partition.
     *
     * @param name display name
     * @param vocalName whether the name is announced by voice
     * @param type this partition's behavior type
     * @param entryDelay entry delay, in seconds
     * @param exitDelay exit delay, in seconds
     */
    public Partition(String name, boolean vocalName, Type type, int entryDelay, int exitDelay)
    {
        setName(name);
        setVocalName(vocalName);
        setType(type);
        setEntryDelay(entryDelay);
        setExitDelay(exitDelay);
    }

    /**
     * Returns the display name of this partition.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this partition.
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
     * Returns whether this partition's name is announced by voice.
     *
     * @return {@code true} if vocal
     */
    public boolean isVocalName()
    {
        return vocalName;
    }

    /**
     * Sets whether this partition's name is announced by voice.
     *
     * @param vocalName {@code true} if vocal
     */
    public void setVocalName(boolean vocalName)
    {
        this.vocalName = vocalName;
    }

    /**
     * Returns this partition's behavior type.
     *
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Sets this partition's behavior type.
     *
     * @param type the type to set, not {@code null} and not {@link Type#UNKNOWN}
     * @throws IllegalArgumentException if {@code type} is {@code null} or {@code UNKNOWN}
     */
    public void setType(Type type)
    {
        if (type == null) throw new IllegalArgumentException("Missing mandatory type");
        if (type == Type.UNKNOWN) throw new IllegalArgumentException("Wrong type UNKNOWN");

        this.type = type;
    }

    /**
     * Returns the entry delay, in seconds.
     *
     * @return the entry delay
     */
    public int getEntryDelay()
    {
        return entryDelay;
    }

    /**
     * Sets the entry delay.
     *
     * @param entryDelay the delay to set, in seconds, non-negative
     * @throws IllegalArgumentException if negative
     */
    public void setEntryDelay(int entryDelay)
    {
        if (entryDelay < 0) throw new IllegalArgumentException("Wrong entry delay " + entryDelay);

        this.entryDelay = entryDelay;
    }

    /**
     * Returns the exit delay, in seconds.
     *
     * @return the exit delay
     */
    public int getExitDelay()
    {
        return exitDelay;
    }

    /**
     * Sets the exit delay.
     *
     * @param exitDelay the delay to set, in seconds, non-negative
     * @throws IllegalArgumentException if negative
     */
    public void setExitDelay(int exitDelay)
    {
        if (exitDelay < 0) throw new IllegalArgumentException("Wrong exit delay " + exitDelay);

        this.exitDelay = exitDelay;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{name=").append(name).append(", vocalName=").append(vocalName).append(", type=").append(type
               ).append(", entryDelay=").append(entryDelay).append(", exitDelay=").append(exitDelay).append("}");

        return sb.toString();
    }
}
