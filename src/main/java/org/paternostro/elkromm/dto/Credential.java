package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * Common base for the two kinds of access credential recognized by the
 * panel: keypad-code {@link User}s and proximity {@link Key}s.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public abstract class Credential implements Serializable, Comparable<Credential>
{
    /** How and when a credential is allowed to arm/disarm its associated partitions. */
    public enum Enabling {
        /** The credential cannot arm/disarm anything. */
        DISABLED(0x00),
        /** The credential can arm/disarm normally. */
        ENABLED(0x01),
        /** The credential can arm/disarm even when it would otherwise be restricted (e.g. by a day class schedule). */
        ALWAYS_ENABLED(0x02),
        /** Sentinel for a raw value with no known meaning; never a valid value to set. */
        UNKNOWN(0xFF);         // should never happen...

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
    }

    protected int       ordinal;
    protected String    name;
    protected Enabling  enabling;
    protected boolean[] associatedPartitions;

    /**
     * Creates a new credential.
     *
     * @param ordinal 1-based position of this credential in its array (users and keys are numbered independently)
     * @param name display name
     * @param enabling arming/disarming enabling mode
     * @param associatedPartitions per-partition association flags, length {@link org.paternostro.elkromm.ElkrommFacade#MAX_PARTITIONS}
     */
    public Credential(int ordinal, String name, Enabling enabling, boolean[] associatedPartitions)
    {
        setOrdinal(ordinal);
        setName(name);
        setEnabling(enabling);
        setAssociatedPartitions(associatedPartitions);
    }

    /**
     * Returns the 1-based ordinal of this credential.
     *
     * @return the ordinal
     */
    public int getOrdinal()
    {
        return ordinal;
    }

    /**
     * Sets the 1-based ordinal of this credential.
     *
     * @param ordinal the ordinal to set, in range [1, {@link org.paternostro.elkromm.ElkrommFacade#MAX_CREDENTIALS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setOrdinal(int ordinal)
    {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong ordinal " + ordinal + ", expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.ordinal = ordinal;
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
        return enabling.getValue();
    }

    /**
     * Returns this credential's enabling mode.
     *
     * @return the enabling mode
     */
    public Enabling getEnabling()
    {
        return enabling;
    }

    /**
     * Sets this credential's enabling mode.
     *
     * @param enabling the mode to set, not {@code null} and not {@link Enabling#UNKNOWN}
     * @throws IllegalArgumentException if {@code enabling} is {@code null} or {@code UNKNOWN}
     */
    public void setEnabling(Enabling enabling)
    {
        if (enabling == null) throw new IllegalArgumentException("Missing mandatory enabling");
        if (enabling == Enabling.UNKNOWN) throw new IllegalArgumentException("Wrong enabling UNKNOWN");

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
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{ordinal=").append(ordinal).append(", name=").append(name).append(", enabling=").append(enabling
               ).append(", associatedPartitions=").append(Arrays.toString(associatedPartitions)).append("}");
        
        return sb.toString();
    }

    /**
     * Compares two credentials by their ordinal.
     *
     * @param o the other credential to compare against
     * @return the result of comparing the two ordinals
     */
    @Override
    public int compareTo(Credential o) {
        return Integer.compare(this.getOrdinal(), o.getOrdinal());
    }
}
