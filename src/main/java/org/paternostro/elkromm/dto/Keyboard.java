package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A keypad ("tastiera"): the panel's local user interface unit, with its own
 * bus address, two onboard inputs, and enablings for sound/audio feedback.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Keyboard implements Serializable, Comparable<Keyboard>
{
    /**
     * Bitmask of keypad sound/behavior enablings.
     * <p>
     * {@code KE_NONE} and {@code KE_ALL} are convenience aliases, not real
     * single-bit values.
     */
    // bitmask!
    public enum Enablings {
        KE_NONE(0x00), // not a real bitmask
        /** Door gong sound. */
        KE_GONG(0x01),
        /** Entry delay sound. */
        KE_ENTRY(0x02),
        /** Exit delay sound. */
        KE_EXIT(0x04),
        /** Masking detection. */
        KE_MASKING(0x08),
        /** Fire alarm sound. */
        KE_FIRE(0x10),
        /** Panic sound. */
        KE_PANIC(0x20),
        /** Help message enabled. */
        KE_HELP(0x40),
        KE_ALL(0x7F); // not a real bitmask

        private byte value;

        Enablings(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this enabling.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Enablings} matching an exact raw bitmask value.
         *
         * @param value the raw value to look up
         * @return the matching enabling, or {@code null} if none matches
         */
        public static Enablings valueOf(byte value) {
            Enablings  retval = null;

            for (Enablings pivot : Enablings.values()) {
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
        public static boolean isEnabled(byte value, Enablings enabling) {
            return (value & enabling.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid enabling bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #KE_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((KE_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /** Bitmask of keypad audio (voice synthesis) capabilities. */
    // bitmask!
    public enum AudioFeatures {
        KA_NONE(0x00), // not a real bitmask
        /** The keypad hardware is capable of voice synthesis. */
        KA_CAPABLE(0x02),
        /** Voice synthesis is enabled. */
        KA_ENABLED(0x04),
        KA_ALL(0x06);

        private byte value;

        AudioFeatures(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this audio feature.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code AudioFeatures} matching an exact raw bitmask value.
         *
         * @param value the raw value to look up
         * @return the matching feature, or {@code null} if none matches
         */
        public static AudioFeatures valueOf(byte value) {
            AudioFeatures  retval = null;

            for (AudioFeatures pivot : AudioFeatures.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        /**
         * Checks whether a given feature's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param flag the single feature bit to look for
         * @return {@code true} if {@code flag}'s bit is set in {@code value}
         */
        public static boolean is(byte value, AudioFeatures flag) {
            return (value & flag.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid feature bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #KA_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((KA_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    private int             address;
    private String          version;
    private Input           firstInput;
    private Input           secondInput;
    private byte            enablings;
    private boolean[]       associatedPartitions;
    private byte            audioFeatures;
    private String          name;

    /**
     * Creates a new keypad.
     *
     * @param address bus address of this keypad, in range [1, {@link ElkrommFacade#MAX_KEYPADS}]
     * @param version firmware version string
     * @param firstInput configuration of the keypad's first onboard input
     * @param secondInput configuration of the keypad's second onboard input
     * @param enablings a valid {@link Enablings} bitmask
     * @param associatedPartitions per-partition association flags
     * @param audioFeatures a valid {@link AudioFeatures} bitmask
     * @param name display name
     */
    public Keyboard(int address, String version, Input firstInput, Input secondInput, byte enablings, boolean[] associatedPartitions, byte audioFeatures, String name)
    {
        setAddress(address);
        setVersion(version);
        setFirstInput(firstInput);
        setSecondInput(secondInput);
        setEnablings(enablings);
        setAssociatedPartitions(associatedPartitions);
        setAudioFeatures(audioFeatures);
        setName(name);
    }

    /**
     * Returns this keypad's bus address.
     *
     * @return the address
     */
    public int getAddress()
    {
        return address;
    }

    /**
     * Sets this keypad's bus address.
     *
     * @param address the address to set, in range [1, {@link ElkrommFacade#MAX_KEYPADS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setAddress(int address)
    {
        if (address < 1 || address > ElkrommFacade.MAX_KEYPADS) throw new IllegalArgumentException("Wrong address value, expected between 1 and " + ElkrommFacade.MAX_KEYPADS + ", found " + address);

        this.address = address;
    }

    /**
     * Returns this keypad's firmware version string.
     *
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets this keypad's firmware version string.
     *
     * @param version the version to set, not {@code null}
     * @throws IllegalArgumentException if {@code version} is {@code null}
     */
    public void setVersion(String version)
    {
        if (version == null) throw new IllegalArgumentException("Missing mandatory version");

        this.version = version;
    }

    /**
     * Returns the configuration of this keypad's first onboard input.
     *
     * @return the first input
     */
    public Input getFirstInput()
    {
        return firstInput;
    }

    /**
     * Sets the configuration of this keypad's first onboard input.
     *
     * @param firstInput the input to set, not {@code null}
     * @throws IllegalArgumentException if {@code firstInput} is {@code null}
     */
    public void setFirstInput(Input firstInput)
    {
        if (firstInput == null) throw new IllegalArgumentException("Missing mandatory input");

        this.firstInput = firstInput;
    }

    /**
     * Returns the configuration of this keypad's second onboard input.
     *
     * @return the second input
     */
    public Input getSecondInput()
    {
        return secondInput;
    }

    /**
     * Sets the configuration of this keypad's second onboard input.
     *
     * @param secondInput the input to set, not {@code null}
     * @throws IllegalArgumentException if {@code secondInput} is {@code null}
     */
    public void setSecondInput(Input secondInput)
    {
        if (secondInput == null) throw new IllegalArgumentException("Missing mandatory input");

        this.secondInput = secondInput;
    }

    /**
     * Returns this keypad's raw sound/behavior enablings bitmask.
     *
     * @return the {@link Enablings} bitmask
     */
    public byte getEnablings()
    {
        return enablings;
    }

    /**
     * Sets this keypad's sound/behavior enablings bitmask.
     *
     * @param enablings a valid {@link Enablings} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setEnablings(byte enablings)
    {
        if (!Enablings.isValid(enablings)) throw new IllegalArgumentException(String.format("Invalid enabling bitmap 0x%02x", enablings));

        this.enablings = enablings;
    }

    /**
     * Returns a copy of this keypad's per-partition association flags.
     *
     * @return the association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Sets this keypad's per-partition association flags.
     *
     * @param associatedPartitions the flags to set, not {@code null}
     * @throws IllegalArgumentException if {@code associatedPartitions} is {@code null}
     */
    public void setAssociatedPartitions(boolean[] associatedPartitions)
    {
        if (associatedPartitions == null) throw new IllegalArgumentException("Missing mandatory associated partitions");

        this.associatedPartitions = Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Returns this keypad's raw audio features bitmask.
     *
     * @return the {@link AudioFeatures} bitmask
     */
    public byte getAudioFeatures()
    {
        return audioFeatures;
    }

    /**
     * Sets this keypad's audio features bitmask.
     *
     * @param audioFeatures a valid {@link AudioFeatures} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setAudioFeatures(byte audioFeatures)
    {
        if (!AudioFeatures.isValid(audioFeatures)) throw new IllegalArgumentException(String.format("Invalid audio features bitmap 0x%02x", audioFeatures));

        this.audioFeatures = audioFeatures;
    }

    /**
     * Returns the display name of this keypad.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this keypad.
     *
     * @param name the name to set, not {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

        this.name = name;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{address=").append(address).append(", version=").append(version).append(", firstInput=").append(firstInput).append(", secondInput="
               ).append(secondInput).append(", enablings=").append(enablings).append(", associatedPartitions="
               ).append(Arrays.toString(associatedPartitions)).append(", audioFeatures=").append(audioFeatures).append(", name=").append(name).append("}");
        
        return sb.toString();
    }

    /**
     * Compares two keypads by their bus address.
     *
     * @param o the other keypad to compare against
     * @return the result of comparing the two addresses
     */
    @Override
    public int compareTo(Keyboard o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
