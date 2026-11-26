package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

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
    public static final Partition   UNUSED = new Partition(ElkrommFacade.DEFAULT_NAME, false, Type.STANDARD, EntryExitDelay.PD_DISABLED, EntryExitDelay.PD_DISABLED);

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

    /**
     * The delay a partition waits on entry/exit before triggering an alarm
     *
     * @since 0.6
     */
    public enum EntryExitDelay {
        /** Immediate alarm */
        PD_DISABLED((byte)0),
        /** Ten seconds delay */
        PD_TEN_SECONDS((byte)10),
        /** Twenty seconds delay */
        PD_TWENTY_SECONDS((byte)20),
        /** Thirty seconds delay */
        PD_THIRTY_SECONDS((byte)30),
        /** Sixty seconds delay */
        PD_ONE_MINUTE((byte)60),
        /** Ninety seconds delay */
        PD_ONE_MINUTE_THIRTY_SECONDS((byte)90),
        /** Three hundred seconds delay. <b>WARNING:</b> this is the only value whose raw value (230) does not match its duration in seconds. */
        PD_FIVE_MINUTES((byte)230);

        private final byte value;

        EntryExitDelay(byte value) {
            this.value = value;
        }

        /**
         * Returns the raw byte value of this delay.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code EntryExitDelay} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching delay, or {@code null} if none matches
         */
        public static EntryExitDelay valueOf(byte value) {
            EntryExitDelay  retval = null;

            for (EntryExitDelay pivot : values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private String          name;
    private boolean         vocalName;
    private Type            type;
    private EntryExitDelay  entryDelay;
    private EntryExitDelay  exitDelay;

    /**
     * Creates a new partition.
     *
     * @param name display name
     * @param vocalName whether the name is announced by voice
     * @param type this partition's behavior type
     * @param entryDelay entry delay
     * @param exitDelay exit delay
     */
    public Partition(String name, boolean vocalName, Type type, EntryExitDelay entryDelay, EntryExitDelay exitDelay)
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
     * Returns the entry delay.
     *
     * @return the entry delay
     */
    public EntryExitDelay getEntryDelay()
    {
        return entryDelay;
    }

    /**
     * Sets the entry delay.
     *
     * @param entryDelay the delay to set, not {@code null}
     * @throws IllegalArgumentException if {@code entryDelay} is {@code null}
     */
    public void setEntryDelay(EntryExitDelay entryDelay)
    {
        if (entryDelay == null) throw new IllegalArgumentException("Missing mandatory entryDelay");

        this.entryDelay = entryDelay;
    }

    /**
     * Returns the exit delay.
     *
     * @return the exit delay
     */
    public EntryExitDelay getExitDelay()
    {
        return exitDelay;
    }

    /**
     * Sets the exit delay.
     *
     * @param exitDelay the delay to set, not {@code null}
     * @throws IllegalArgumentException if {@code exitDelay} is {@code null}
     */
    public void setExitDelay(EntryExitDelay exitDelay)
    {
        if (exitDelay == null) throw new IllegalArgumentException("Missing mandatory exitDelay");

        this.exitDelay = exitDelay;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{name=").append(name).append(", vocalName=").append(vocalName).append(", type=").append(type
               ).append(", entryDelay=").append(entryDelay).append(", exitDelay=").append(exitDelay).append("}");

        return sb.toString();
    }
}
