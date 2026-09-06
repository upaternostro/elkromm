package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

/**
 * The weekly time programmer configuration: which day-of-week maps to which
 * {@link DayClass}, plus the default scheduled commands for each day class.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class TimeProgrammer implements Serializable
{
    private Command[]   workingDaysCommands;
    private Command[]   preHolidayDaysCommands;
    private Command[]   holidayDaysCommands;
    private DayClass[]  dayClasses;

    /**
     * Creates a new time programmer configuration.
     *
     * @param workingDaysCommands scheduled commands for working days, length {@link ElkrommFacade#NUM_COMMANDS}
     * @param preHolidayDaysCommands scheduled commands for pre-holiday days, length {@link ElkrommFacade#NUM_COMMANDS}
     * @param holidayDaysCommands scheduled commands for holidays, length {@link ElkrommFacade#NUM_COMMANDS}
     * @param dayClasses day class for each day of the week, length 7 (Monday first)
     */
    public TimeProgrammer(Command[] workingDaysCommands, Command[] preHolidayDaysCommands, Command[] holidayDaysCommands, DayClass[] dayClasses)
    {
        setWorkingDaysCommands(workingDaysCommands);
        setPreHolidayDaysCommands(preHolidayDaysCommands);
        setHolidayDaysCommands(holidayDaysCommands);
        setDayClasses(dayClasses);
    }

    /**
     * Returns the scheduled commands for working days.
     *
     * @return the commands, length {@link ElkrommFacade#NUM_COMMANDS}
     */
    public Command[] getWorkingDaysCommands() {
        return workingDaysCommands;
    }

    /**
     * Sets the scheduled commands for working days.
     *
     * @param workingDaysCommands the commands to set, length {@link ElkrommFacade#NUM_COMMANDS}
     * @throws IllegalArgumentException if {@code null} or the wrong length
     */
    public void setWorkingDaysCommands(Command[] workingDaysCommands) {
        if (workingDaysCommands == null) throw new IllegalArgumentException("Missing mandatory workingDaysCommands");
        if (workingDaysCommands.length != ElkrommFacade.NUM_COMMANDS) throw new IllegalArgumentException("Wrong workingDaysCommands length");

        this.workingDaysCommands = workingDaysCommands;
    }

    /**
     * Returns the scheduled commands for pre-holiday days.
     *
     * @return the commands, length {@link ElkrommFacade#NUM_COMMANDS}
     */
    public Command[] getPreHolidayDaysCommands() {
        return preHolidayDaysCommands;
    }

    /**
     * Sets the scheduled commands for pre-holiday days.
     *
     * @param preHolidayDaysCommands the commands to set, length {@link ElkrommFacade#NUM_COMMANDS}
     * @throws IllegalArgumentException if {@code null} or the wrong length
     */
    public void setPreHolidayDaysCommands(Command[] preHolidayDaysCommands) {
        if (preHolidayDaysCommands == null) throw new IllegalArgumentException("Missing mandatory preHolidayDaysCommands");
        if (preHolidayDaysCommands.length != ElkrommFacade.NUM_COMMANDS) throw new IllegalArgumentException("Wrong preHolidayDaysCommands length");

        this.preHolidayDaysCommands = preHolidayDaysCommands;
    }

    /**
     * Returns the scheduled commands for holidays.
     *
     * @return the commands, length {@link ElkrommFacade#NUM_COMMANDS}
     */
    public Command[] getHolidayDaysCommands() {
        return holidayDaysCommands;
    }

    /**
     * Sets the scheduled commands for holidays.
     *
     * @param holidayDaysCommands the commands to set, length {@link ElkrommFacade#NUM_COMMANDS}
     * @throws IllegalArgumentException if {@code null} or the wrong length
     */
    public void setHolidayDaysCommands(Command[] holidayDaysCommands) {
        if (holidayDaysCommands == null) throw new IllegalArgumentException("Missing mandatory holidayDaysCommands");
        if (holidayDaysCommands.length != ElkrommFacade.NUM_COMMANDS) throw new IllegalArgumentException("Wrong holidayDaysCommands length");

        this.holidayDaysCommands = holidayDaysCommands;
    }

    /**
     * Returns the day class of each day of the week.
     *
     * @return the day classes, length 7 (Monday first)
     */
    public DayClass[] getDayClasses() {
        return dayClasses;
    }

    /**
     * Sets the day class of each day of the week.
     *
     * @param dayClasses the day classes to set, length 7 (Monday first)
     * @throws IllegalArgumentException if {@code null} or not length 7
     */
    public void setDayClasses(DayClass[] dayClasses) {
        if (dayClasses == null) throw new IllegalArgumentException("Missing mandatory dayClasses");
        if (dayClasses.length != 7) throw new IllegalArgumentException("Wrong dayClasses length");

        this.dayClasses = dayClasses;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{workingDaysCommands=").append(Arrays.toString(workingDaysCommands)
               ).append(", preHolidayDaysCommands=").append(Arrays.toString(preHolidayDaysCommands)).append(", holidayDaysCommands="
               ).append(Arrays.toString(holidayDaysCommands)).append(", dayClasses=").append(Arrays.toString(dayClasses)).append("}");
        
        return sb.toString();
    }
}
