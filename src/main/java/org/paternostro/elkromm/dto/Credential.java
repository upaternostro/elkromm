package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.Input.Flags;

/**
 * Common base for the two kinds of access credential recognized by the
 * panel: keypad-code {@link User}s and proximity {@link Key}s.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public abstract class Credential implements Serializable
{
    /** Bitmask to identify when a credential is allowed to arm/disarm its associated partitions.
     * 
     * {@code NONE} and {@code ALL} are convenience aliases, not real
     * single-bit values: {@code NONE} means no bit set, {@code ALL}
     * means every bit set.
     */
    // bitmask!
    public enum Enabling {
        NONE(0x00), // not a real bitmask
        /** The credential cannot arm/disarm anything. */
        DISABLED(0x00),
        /** The credential can arm/disarm normally. */
        ENABLED(0x01),
        /** The credential can arm/disarm even when it would otherwise be restricted (e.g. by a day class schedule). */
        ALWAYS_ENABLED(0x02),
        ALL(0x03); // not a real bitmask

        private byte    value;

        Enabling(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this enabling mode.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code Enabling} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching {@code Enabling}, or {@code null} if none matches
         */
        public static Enabling valueOf(byte value) {
            Enabling    retval = null;

            for (Enabling pivot : Enabling.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        /**
         * Checks whether a given enabling's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param enabling the single enabling bit to look for
         * @return {@code true} if {@code enabling}'s bit is set in {@code value}
         */
        public static boolean is(byte value, Enabling enabling) {
            return (value & enabling.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid enabling bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    protected String    name;
    protected byte      enabling;
    protected boolean[] associatedPartitions;

    /**
     * Creates a new credential.
     *
     * @param name display name
     * @param enabling arming/disarming enabling bitmask
     * @param associatedPartitions per-partition association flags, length {@link org.paternostro.elkromm.ElkrommFacade#MAX_PARTITIONS}
     */
    public Credential(String name, byte enabling, boolean[] associatedPartitions)
    {
        setName(name);
        setEnabling(enabling);
        setAssociatedPartitions(associatedPartitions);
    }

    /**
     * Returns the display name of this credential.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this credential.
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
     * Returns the raw byte value of this credential's {@link Enabling}.
     *
     * @return the raw enabling value
     */
    public byte getEnablingValue()
    {
        return enabling;
    }

    /**
     * Returns this credential's enabling mode.
     *
     * @return the enabling mode bitmask
     */
    public byte getEnabling()
    {
        return enabling;
    }

    /**
     * Sets this credential's enabling mode bitmask.
     *
     * @param enabling the mode to set, not {@code null} and not {@link Enabling#UNKNOWN}
     * @throws IllegalArgumentException if {@code enabling} is {@code null} or {@code UNKNOWN}
     */
    public void setEnabling(byte enabling)
    {
        if (!Enabling.isValid(enabling)) throw new IllegalArgumentException(String.format("Invalid enabling bitmap 0x%02x", enabling));

        this.enabling = enabling;
    }

    /**
     * Returns a copy of this credential's per-partition association flags.
     *
     * @return the association flags, length {@link org.paternostro.elkromm.ElkrommFacade#MAX_PARTITIONS}
     */
    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Sets this credential's per-partition association flags.
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
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{name=").append(name).append(", enabling=").append(enabling
               ).append(", associatedPartitions=").append(Arrays.toString(associatedPartitions)).append("}");
        
        return sb.toString();
    }
}
