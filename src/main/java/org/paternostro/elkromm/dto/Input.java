package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A logical input (sensor): a door/window contact, PIR, shock/roller
 * sensor, key switch, or any other supervised alarm point, on the main
 * board or an {@link Expansion}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Input implements Serializable, Comparable<Input>
{
    /** Electrical wiring configuration (balanced/normally open-closed) of an input. */
    public enum Configuration {
        /** Input not used. */
        IC_NOT_USED(0x00),
        /** Normally-closed contact. */
        IC_NORMALLY_CLOSED(0x01),
        /** Normally-open contact. */
        IC_NORMALLY_OPEN(0x02),
        /** Normally-closed, single balanced (end-of-line resistor) contact. */
        IC_NORMALLY_CLOSED_BALANCED(0x03),
        /** Normally-closed, double balanced contact. */
        IC_NORMALLY_CLOSED_DOUBLE_BALANCED(0x04),
        /** Shock sensor (see {@link Sensitivity}). */
        IC_SHOCK(0x07),
        /** Roller shutter sensor (see {@link Sensitivity}). */
        IC_ROLLER(0x08);

        private byte value;

        Configuration(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this configuration.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Configuration} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching configuration, or {@code null} if none matches
         */
        public static Configuration valueOf(byte value) {
            Configuration   retval = null;

            for (Configuration pivot : Configuration.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** What kind of alarm/event this input reports. */
    public enum Specialization {
        /** Immediate alarm, no entry delay. */
        IS_IMMEDIATE(0x00),
        /** Delayed alarm, subject to entry delay. */
        IS_DELAYED(0x01),
        /** First entry point (starts the entry delay). */
        IS_FIRST_ENTRY(0x02),
        /** "Way" input, along the path to/from an entry point. */
        IS_WAY(0x03),
        /** Last exit point (ends the exit delay). */
        IS_LAST_EXIT(0x04),
        /** Both first entry and last exit. */
        IS_FIRST_ENTRY_LAST_EXIT(0x05),
        /** Fire detector. */
        IS_FIRE(0x06),
        /** Technical alarm, first type. */
        IS_TECHNO_TYPE_1(0x07),
        /** Technical alarm, second type. */
        IS_TECHNO_TYPE_2(0x08),
        /** Technical alarm, third type. */
        IS_TECHNO_TYPE_3(0x09),
        /** Pre-alarm. */
        IS_PRE_ALARM(0x0a),
        /** Panic button. */
        IS_PANIC(0x0b),
        /** Silent panic button. */
        IS_SILENT_PANIC(0x0c),
        /** Hold-up button. */
        IS_HOLD_UP(0x0d),
        /** Fire alarm reset. */
        IS_FIRE_RESET(0x0e),
        /** Emergency button. */
        IS_EMERGENCY(0x0f),
        /** Key switch (arms/disarms directly). */
        IS_KEY(0x10),
        /** Tampering. */
        IS_TAMPERING(0x11),
        /** Generic fault. */
        IS_FAULT(0x12),
        /** Input self-test. */
        IS_INPUT_TEST(0x13);

        private byte value;

        Specialization(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this specialization.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Specialization} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching specialization, or {@code null} if none matches
         */
        public static Specialization valueOf(byte value) {
            Specialization  retval = null;

            for (Specialization pivot : Specialization.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /**
     * Detection sensitivity, meaningful only for
     * {@link Configuration#IC_SHOCK} and {@link Configuration#IC_ROLLER}
     * inputs.
     */
    // Solo per IC_SHOCK e IC_ROLLER
    public enum Sensitivity {
        IS_LOW(0x80),
        IS_MEDIUM(0x40),
        IS_HIGH(0x00);

        private byte value;

        Sensitivity(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this sensitivity.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Sensitivity} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching sensitivity, or {@code null} if none matches
         */
        public static Sensitivity valueOf(byte value) {
            Sensitivity   retval = null;

            for (Sensitivity pivot : Sensitivity.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /**
     * Bitmask of behavioral flags for an input.
     * <p>
     * {@code IF_NONE} and {@code IF_ALL} are convenience aliases, not real
     * single-bit values.
     */
    // bitmask!
    public enum Flags {
        IF_NONE(0x00),
        /** This input can be individually excluded from the alarm logic. */
        IF_EXCLUSION_ENABLED(0x01),
        /** Requires two separate triggers (double release) before alarming. */
        IF_DOUBLE_RELEASE(0x02),
        /** Alarms if any of its associated partitions is armed (OR), rather than all of them (AND). */
        IF_OR_SECTORS(0x08),
        IF_ALL(0x0b);

        private byte value;

        Flags(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this flag.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Flags} matching an exact raw bitmask value.
         *
         * @param value the raw value to look up
         * @return the matching flags, or {@code null} if none matches
         */
        public static Flags valueOf(byte value) {
            Flags   retval = null;

            for (Flags pivot : Flags.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        /**
         * Checks whether a given flag's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param flag the single flag bit to look for
         * @return {@code true} if {@code flag}'s bit is set in {@code value}
         */
        public static boolean is(byte value, Flags flag) {
            return (value & flag.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid flag bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #IF_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((IF_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /**
     * Auxiliary functions an input can trigger.
     * <p>
     * Not currently exposed as a field of {@link Input} itself; kept here
     * as the protocol-level enum pending further reverse engineering.
     */
    public enum Functions {
        /** Triggers a "no movement" timeout function. */
        IF_NO_MOVEMENT(0x01),
        /** Triggers a door gong. */
        IF_GONG(0x02),
        /** Triggers a courtesy light. */
        IF_COURTESY_LIGHT(0x04),
        /** Triggers an automatic door opener. */
        IF_OPEN_DOOR(0x08);

        private byte value;

        Functions(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this function.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Functions} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching function, or {@code null} if none matches
         */
        public static Functions valueOf(byte value) {
            Functions   retval = null;

            for (Functions pivot : Functions.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** How long an input's alarm condition must persist before it's reported. */
    public enum Delay {
        ID_5_SECS(0x00),
        ID_10_SECS(0x01),
        ID_30_SECS(0x02),
        ID_60_SECS(0x03),
        ID_90_SECS(0x04),
        ID_5_MINS(0x05),
        ID_20_SECS(0x06);

        private byte value;

        Delay(int value)
        {
            this.value = (byte)value;
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
         * Looks up the {@code Delay} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching delay, or {@code null} if none matches
         */
        public static Delay valueOf(byte value) {
            Delay   retval = null;

            for (Delay pivot : Delay.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** Which CCTV camera (if any) is tied to this input. */
    public enum Video {
        /** No camera tied to this input. */
        IV_NONE(0x00),
        IV_CAMERA_1(0x10),
        IV_CAMERA_2(0x20),
        IV_CAMERA_3(0x40),
        IV_CAMERA_4(0x80);

        private byte value;

        Video(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this camera selection.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Video} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching camera selection, or {@code null} if none matches
         */
        public static Video valueOf(byte value) {
            Video   retval = null;

            for (Video pivot : Video.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private int             logicNumber;
    private Configuration   configuration;
    private Specialization  specialization;
    private Sensitivity     sensitivity;
    private byte            flags;
    private Video           video;
    private boolean[]       associatedPartitions;
    private String          name;
    private Delay           delay;

    /**
     * Creates a new input.
     *
     * @param logicNumber this input's logical number, in range [1, {@link ElkrommFacade#MAX_LOGICAL_INPUTS}]
     * @param configuration electrical wiring configuration
     * @param specialization what kind of alarm/event this input reports
     * @param sensitivity detection sensitivity ({@code null} defaults to {@link Sensitivity#IS_HIGH}), relevant only for shock/roller inputs
     * @param flags a valid {@link Flags} bitmask
     * @param video which camera is tied to this input ({@code null} defaults to {@link Video#IV_NONE})
     * @param associatedPartitions per-partition association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     * @param name display name
     * @param delay how long the alarm condition must persist ({@code null} defaults to {@link Delay#ID_5_SECS})
     */
    public Input(int logicNumber, Configuration configuration, Specialization specialization, Sensitivity sensitivity, byte flags, Video video, boolean[] associatedPartitions, String name, Delay delay)
    {
        setLogicNumber(logicNumber);
        setConfiguration(configuration);
        setSpecialization(specialization);
        setSensitivity(sensitivity);
        setFlags(flags);
        setVideo(video);
        setAssociatedPartitions(associatedPartitions);
        setName(name);
        setDelay(delay);
    }

    /**
     * Returns this input's logical number.
     *
     * @return the logical number
     */
    public int getLogicNumber()
    {
        return logicNumber;
    }

    /**
     * Sets this input's logical number.
     *
     * @param logicNumber the number to set, in range [1, {@link ElkrommFacade#MAX_LOGICAL_INPUTS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setLogicNumber(int logicNumber)
    {
        if (logicNumber < 1 || logicNumber > ElkrommFacade.MAX_LOGICAL_INPUTS) throw new IllegalArgumentException("Wrong logic number value, expected between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS + ", found " + logicNumber);

        this.logicNumber = logicNumber;
    }

    /**
     * Returns this input's electrical wiring configuration.
     *
     * @return the configuration
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets this input's electrical wiring configuration.
     *
     * @param configuration the configuration to set, not {@code null}
     * @throws IllegalArgumentException if {@code configuration} is {@code null}
     */
    public void setConfiguration(Configuration configuration)
    {
        if (configuration == null) throw new IllegalArgumentException("Missing mandatory configuration");

        this.configuration = configuration;
    }

    /**
     * Returns what kind of alarm/event this input reports.
     *
     * @return the specialization
     */
    public Specialization getSpecialization()
    {
        return specialization;
    }

    /**
     * Sets what kind of alarm/event this input reports.
     *
     * @param specialization the specialization to set, not {@code null}
     * @throws IllegalArgumentException if {@code specialization} is {@code null}
     */
    public void setSpecialization(Specialization specialization)
    {
        if (specialization == null) throw new IllegalArgumentException("Missing mandatory specialization");

        this.specialization = specialization;
    }

    /**
     * Returns this input's detection sensitivity.
     *
     * @return the sensitivity
     */
    public Sensitivity getSensitivity()
    {
        return sensitivity;
    }

    /**
     * Sets this input's detection sensitivity.
     *
     * @param sensitivity the sensitivity to set, or {@code null} to default to {@link Sensitivity#IS_HIGH}
     */
    public void setSensitivity(Sensitivity sensitivity)
    {
        if (sensitivity == null) sensitivity = Sensitivity.IS_HIGH;

        this.sensitivity = sensitivity;
    }

    /**
     * Returns this input's raw behavioral flags bitmask.
     *
     * @return the {@link Flags} bitmask
     */
    public byte getFlags()
    {
        return flags;
    }

    /**
     * Sets this input's behavioral flags bitmask.
     *
     * @param flags a valid {@link Flags} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setFlags(byte flags)
    {
        if (!Flags.isValid(flags)) throw new IllegalArgumentException(String.format("Invalid flags bitmap 0x%02x", flags));

        this.flags = flags;
    }

    /**
     * Returns which camera is tied to this input.
     *
     * @return the camera selection
     */
    public Video getVideo()
    {
        return video;
    }

    /**
     * Sets which camera is tied to this input.
     *
     * @param video the camera selection to set, or {@code null} to default to {@link Video#IV_NONE}
     */
    public void setVideo(Video video)
    {
        if (video == null) video = Video.IV_NONE;

        this.video = video;
    }

    /**
     * Returns a copy of this input's per-partition association flags.
     *
     * @return the association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Sets this input's per-partition association flags.
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
     * Returns the display name of this input.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this input.
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
     * Returns how long this input's alarm condition must persist before being reported.
     *
     * @return the delay
     */
    public Delay getDelay()
    {
        return delay;
    }

    /**
     * Sets how long this input's alarm condition must persist before being reported.
     *
     * @param delay the delay to set, or {@code null} to default to {@link Delay#ID_5_SECS}
     */
    public void setDelay(Delay delay)
    {
        if (delay == null) delay = Delay.ID_5_SECS;

        this.delay = delay;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{logicNumber=").append(logicNumber).append(", configuration=").append(configuration).append(", specialization="
               ).append(specialization).append(", sensitivity=").append(sensitivity).append(", flags=").append(flags).append(", video=").append(video
               ).append(", associatedPartitions=").append(Arrays.toString(associatedPartitions)).append(", name=").append(name).append(", delay="
               ).append(delay).append("}");
        
        return sb.toString();
    }

    /**
     * Compares two inputs by their logical number.
     *
     * @param o the other input to compare against
     * @return the result of comparing the two logical numbers
     */
    @Override
    public int compareTo(Input o)
    {
        return Integer.compare(this.getLogicNumber(), o.getLogicNumber());
    }
}
