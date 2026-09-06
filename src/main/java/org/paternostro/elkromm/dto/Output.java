package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A programmable output: a physical relay on an expansion unit, driven by
 * a system event ({@link Specialization}) and wired {@link Type}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Output implements Serializable, Comparable<Output>
{
    /** Electrical behavior of an output relay. */
    public enum Type {
        /** Output not used. */
        OT_NOT_USED(0x00),
        /** Normally-low (open) output. */
        OT_NORMALLY_LOW(0x01),
        /** Normally-high (closed) output. */
        OT_NORMALLY_HIGH(0x02);

        private byte value;

        Type(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this type.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Type} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching type, or {@code null} if none matches
         */
        public static Type valueOf(byte value) {
            Type  retval = null;

            for (Type pivot : Type.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** Which system event drives an output. */
    public enum Specialization {
        /** Burglary alarm. */
        OS_BURGLAR(0x00),
        /** Pre-alarm. */
        OS_PRE_ALARM(0x01),
        /** Burglary alarm reset. */
        OS_BURGLAR_RESET(0x02),
        /** Tampering. */
        OS_TAMPERING(0x03),
        /** Silent panic. */
        OS_SILENT_PANIC(0x04),
        /** Panic. */
        OS_PANIC(0x05),
        /** Hold-up. */
        OS_HOLD_UP(0x06),
        /** Emergency. */
        OS_EMERGENCY(0x07),
        /** Technical alarm, first type. */
        OS_TECHNO_TYPE_1(0x08),
        /** Technical alarm, second type. */
        OS_TECHNO_TYPE_2(0x09),
        /** Technical alarm, third type. */
        OS_TECHNO_TYPE_3(0x0a),
        /** Fire alarm. */
        OS_FIRE(0x0b),
        /** Fire alarm reset. */
        OS_FIRE_RESET(0x0c),
        /** System fault. */
        OS_SYSTEM_FAULT(0x0d),
        /** Telephone line fault. */
        OS_TEL_FAULT(0x0e),
        /** Low battery. */
        OS_LOW_BATTERY(0x0f),
        /** Mains power lack. */
        OS_LACK_OF_POWER(0x10),
        /** Door gong. */
        OS_GONG(0x11),
        /** Buzzer. */
        OS_BUZZER(0x12),
        /** Partition arming status. */
        OS_PARTIT_STATUS(0x13),
        /** AND combination of time classes. */
        OS_AND_TC(0x14),
        /** OR combination of time classes. */
        OS_OR_TC(0x15),
        /** Arming notice. */
        OS_ARM_NOTICE(0x16),
        /** Input open. */
        OS_OPEN_INPUT(0x17),
        /** Input excluded. */
        OS_INPUT_EXCL(0x18),
        /** Manually commandable output. */
        OS_COMMANDABLE(0x19),
        /** Door open. */
        OS_DOOR_OPEN(0x1a),
        /** Courtesy light. */
        OS_COURT_LIGHT(0x1b),
        /** Generic fault. */
        OS_FAULT(0x1c),
        /** Burglary + tamper combination. */
        OS_BURGLAR_TAMPER(0x1d),
        /** Pulsed output. */
        OS_PULSED_OUTPUT(0x1e);

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

    private int             logicNumber;
    private Type            type;
    private boolean[]       associatedPartitions;
    private Specialization  specialization;
    private String          name;

    /**
     * Creates a new output.
     *
     * @param logicNumber this output's logical number, greater than 0
     * @param type this output's electrical type
     * @param associatedPartitions per-partition association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     * @param specialization which event drives this output
     * @param name display name
     */
    public Output(int logicNumber, Type type, boolean[] associatedPartitions, Specialization specialization, String name)
    {
        setLogicNumber(logicNumber);
        setType(type);
        setAssociatedPartitions(associatedPartitions);
        setSpecialization(specialization);
        setName(name);
    }

    /**
     * Returns this output's logical number.
     *
     * @return the logical number
     */
    public int getLogicNumber()
    {
        return logicNumber;
    }

    /**
     * Sets this output's logical number.
     *
     * @param logicNumber the number to set, greater than 0
     * @throws IllegalArgumentException if not positive
     */
    public void setLogicNumber(int logicNumber)
    {
        if (logicNumber < 1) throw new IllegalArgumentException("Wrong logic number " + logicNumber + ", expected greater than 0");

        this.logicNumber = logicNumber;
    }

    /**
     * Returns this output's electrical type.
     *
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Sets this output's electrical type.
     *
     * @param type the type to set, not {@code null}
     * @throws IllegalArgumentException if {@code type} is {@code null}
     */
    public void setType(Type type)
    {
        if (type == null) throw new IllegalArgumentException("Missing mandatory type");

        this.type = type;
    }

    /**
     * Returns a copy of this output's per-partition association flags.
     *
     * @return the association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Sets this output's per-partition association flags.
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
     * Returns which event drives this output.
     *
     * @return the specialization
     */
    public Specialization getSpecialization()
    {
        return specialization;
    }

    /**
     * Sets which event drives this output.
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
     * Returns the display name of this output.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this output.
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
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{logicNumber=").append(logicNumber).append(", type=").append(type).append(", associatedPartitions="
               ).append(Arrays.toString(associatedPartitions)).append(", specialization=").append(specialization).append(", name=").append(name).append("}");
        
        return sb.toString();
    }

    /**
     * Compares two outputs by their logical number.
     *
     * @param o the other output to compare against
     * @return the result of comparing the two logical numbers
     */
    @Override
    public int compareTo(Output o)
    {
        return Integer.compare(this.getLogicNumber(), o.getLogicNumber());
    }
}
