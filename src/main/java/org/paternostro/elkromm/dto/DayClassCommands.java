package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class DayClassCommands implements Serializable, Comparable<DayClassCommands>
{
    public enum DayClass {
        DCCDC_WORKING_DAY(0x00),
        DCCDC_PRE_HOLIDAY(0x01),
        DCCDC_HOLIDAY(0x02);

        private byte value;

        DayClass(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

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

    public DayClassCommands(DayClass dayClass, Command[] commands)
    {
        setDayClass(dayClass);
        setCommands(commands);
    }

    public DayClass getDayClass() {
        return dayClass;
    }

    public void setDayClass(DayClass dayClass) {
        if (dayClass == null) throw new IllegalArgumentException("Missing mandatory dayClass");

        this.dayClass = dayClass;
    }

    public Command[] getCommands() {
        return Arrays.copyOf(commands, ElkrommFacade.NUM_COMMANDS);
    }

    public void setCommands(Command[] commands) {
        if (commands == null) throw new IllegalArgumentException("Missing mandatory commands");
        if (commands.length == 0) throw new IllegalArgumentException("Empty commands array");

        this.commands = Arrays.copyOf(commands, ElkrommFacade.NUM_COMMANDS);
    }

    @Override
    public int compareTo(DayClassCommands o) {
        return dayClass.compareTo(o.dayClass);
    }
}
