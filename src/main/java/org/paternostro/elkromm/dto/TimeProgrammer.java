package org.paternostro.elkromm.dto;

import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

public class TimeProgrammer
{
    private Command[]   workingDaysCommands;
    private Command[]   preHolidayDaysCommands;
    private Command[]   holidayDaysCommands;
    private DayClass[]  dayClasses;

    public TimeProgrammer(Command[] workingDaysCommands, Command[] preHolidayDaysCommands, Command[] holidayDaysCommands, DayClass[] dayClasses)
    {
        setWorkingDaysCommands(workingDaysCommands);
        setPreHolidayDaysCommands(preHolidayDaysCommands);
        setHolidayDaysCommands(holidayDaysCommands);
        setDayClasses(dayClasses);
    }

    public Command[] getWorkingDaysCommands() {
        return workingDaysCommands;
    }

    public void setWorkingDaysCommands(Command[] workingDaysCommands) {
        if (workingDaysCommands == null) throw new IllegalArgumentException("Missing mandatory workingDaysCommands");
        if (workingDaysCommands.length != 8) throw new IllegalArgumentException("Wrong workingDaysCommands length");

        this.workingDaysCommands = workingDaysCommands;
    }

    public Command[] getPreHolidayDaysCommands() {
        return preHolidayDaysCommands;
    }

    public void setPreHolidayDaysCommands(Command[] preHolidayDaysCommands) {
        if (preHolidayDaysCommands == null) throw new IllegalArgumentException("Missing mandatory preHolidayDaysCommands");
        if (preHolidayDaysCommands.length != 8) throw new IllegalArgumentException("Wrong preHolidayDaysCommands length");

        this.preHolidayDaysCommands = preHolidayDaysCommands;
    }

    public Command[] getHolidayDaysCommands() {
        return holidayDaysCommands;
    }

    public void setHolidayDaysCommands(Command[] holidayDaysCommands) {
        if (holidayDaysCommands == null) throw new IllegalArgumentException("Missing mandatory holidayDaysCommands");
        if (holidayDaysCommands.length != 8) throw new IllegalArgumentException("Wrong holidayDaysCommands length");

        this.holidayDaysCommands = holidayDaysCommands;
    }

    public DayClass[] getDayClasses() {
        return dayClasses;
    }

    public void setDayClasses(DayClass[] dayClasses) {
        if (dayClasses == null) throw new IllegalArgumentException("Missing mandatory dayClasses");
        if (dayClasses.length != 7) throw new IllegalArgumentException("Wrong dayClasses length");

        this.dayClasses = dayClasses;
    }
}
