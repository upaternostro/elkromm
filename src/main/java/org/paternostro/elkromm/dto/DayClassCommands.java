package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * The scheduled commands for a single day class (working day, pre-holiday,
 * or holiday), as used by the weekly {@link TimeProgrammer} and written via
 * the {@code DAY_CLASS_CMDS} single-instance write command.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class DayClassCommands implements Serializable, Comparable<DayClassCommands>
{
    /** Which kind of day a set of scheduled commands applies to. */
    public enum DayClass {
        /** An ordinary working day. */
        DCCDC_WORKING_DAY(0x00),
        /** The day before a holiday. */
        DCCDC_PRE_HOLIDAY(0x01),
        /** A holiday. */
        DCCDC_HOLIDAY(0x02);

        private byte value;

        DayClass(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this day class.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code DayClass} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching day class, or {@code null} if none matches
         */
        public static DayClass valueOf(byte value) {
            DayClass  retval = null;

            for (DayClass pivot : DayClass.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private DayClass    dayClass;
    private Command[]   commands;

    /**
     * Creates a new day class commands schedule.
     *
     * @param dayClass which day class this schedule applies to
     * @param commands the scheduled commands, length {@link ElkrommFacade#NUM_COMMANDS}
     */
    public DayClassCommands(DayClass dayClass, Command[] commands)
    {
        setDayClass(dayClass);
        setCommands(commands);
    }

    /**
     * Returns which day class this schedule applies to.
     *
     * @return the day class
     */
    public DayClass getDayClass() {
        return dayClass;
    }

    /**
     * Sets which day class this schedule applies to.
     *
     * @param dayClass the day class to set, not {@code null}
     * @throws IllegalArgumentException if {@code dayClass} is {@code null}
     */
    public void setDayClass(DayClass dayClass) {
        if (dayClass == null) throw new IllegalArgumentException("Missing mandatory dayClass");

        this.dayClass = dayClass;
    }

    /**
     * Returns a copy of the scheduled commands.
     *
     * @return the commands, length {@link ElkrommFacade#NUM_COMMANDS}
     */
    public Command[] getCommands() {
        return Arrays.copyOf(commands, ElkrommFacade.NUM_COMMANDS);
    }

    /**
     * Sets the scheduled commands.
     *
     * @param commands the commands to set, not {@code null} or empty
     * @throws IllegalArgumentException if {@code commands} is {@code null} or empty
     */
    public void setCommands(Command[] commands) {
        if (commands == null) throw new IllegalArgumentException("Missing mandatory commands");
        if (commands.length == 0) throw new IllegalArgumentException("Empty commands array");

        this.commands = Arrays.copyOf(commands, ElkrommFacade.NUM_COMMANDS);
    }

    /**
     * Compares two schedules by their day class.
     *
     * @param o the other schedule to compare against
     * @return the result of comparing the two day classes
     */
    @Override
    public int compareTo(DayClassCommands o) {
        return dayClass.compareTo(o.dayClass);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{dayClass=").append(dayClass).append(", commands=").append(Arrays.toString(commands)).append("}");

        return sb.toString();
    }
}
