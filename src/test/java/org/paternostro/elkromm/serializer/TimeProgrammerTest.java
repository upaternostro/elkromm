package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Command.Action;
import org.paternostro.elkromm.dto.Command.ObjectType;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class TimeProgrammerTest {
    @Test
    public void test()
    {
        Command[]   workingDayCommands = new Command[8];
        Command[]   preHolidayDayCommands = new Command[8];
        Command[]   holidayDayCommands = new Command[8];
        DayClass[]  dayClasses = { DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_PRE_HOLIDAY, DayClass.DCCDC_HOLIDAY };

        for (int i = 0; i < workingDayCommands.length; i++) {
            workingDayCommands[i] = new Command(i % 2 == 0 ? Action.CA_ENABLE : Action.CA_DISABLE, (byte)(i + 1), i % 2 == 0 ? ObjectType.COT_SECTORS : ObjectType.COT_USER, (byte)(23 - i), (byte)(59 - i));
        }

        for (int i = 0; i < preHolidayDayCommands.length; i++) {
            preHolidayDayCommands[i] = new Command(i % 2 == 0 ? Action.CA_DISABLE : Action.CA_ENABLE, (byte)(i + 1), i % 2 == 0 ? ObjectType.COT_USER : ObjectType.COT_SECTORS, (byte)i, (byte)i);
        }

        for (int i = 0; i < holidayDayCommands.length; i++) {
            holidayDayCommands[i] = new Command(i % 2 == 0 ? Action.CA_ENABLE : Action.CA_DISABLE, (byte)(i + 1), i % 2 == 0 ? ObjectType.COT_SECTORS : ObjectType.COT_USER, (byte)(23 - i), (byte)(59 - i));
        }

        org.paternostro.elkromm.dto.TimeProgrammer  timeProgrammer = new org.paternostro.elkromm.dto.TimeProgrammer(workingDayCommands, preHolidayDayCommands, holidayDayCommands, dayClasses);
        byte[]                                      data = ElkrommFactory.getFactory().getTimeProgrammerSerializer().serialize(timeProgrammer);

        assert data.length == 131 : "Wrong length";

        org.paternostro.elkromm.dto.TimeProgrammer   timeProgrammer2 = ElkrommFactory.getFactory().getTimeProgrammerSerializer().deserialize(data);

        assert timeProgrammer.getWorkingDaysCommands().length == timeProgrammer2.getWorkingDaysCommands().length;
        assert timeProgrammer.getPreHolidayDaysCommands().length == timeProgrammer2.getPreHolidayDaysCommands().length;
        assert timeProgrammer.getHolidayDaysCommands().length == timeProgrammer2.getHolidayDaysCommands().length;
        assert timeProgrammer.getDayClasses().length == timeProgrammer2.getDayClasses().length;

        for (int i = 0; i < timeProgrammer.getWorkingDaysCommands().length; i++) {
            assert timeProgrammer.getWorkingDaysCommands()[i].getAction() == timeProgrammer2.getWorkingDaysCommands()[i].getAction();
            assert timeProgrammer.getWorkingDaysCommands()[i].getObject() == timeProgrammer2.getWorkingDaysCommands()[i].getObject();
            assert timeProgrammer.getWorkingDaysCommands()[i].getObjectType() == timeProgrammer2.getWorkingDaysCommands()[i].getObjectType() : "Ecpected " + timeProgrammer.getWorkingDaysCommands()[i].getObjectType() + " but found " + timeProgrammer2.getWorkingDaysCommands()[i].getObjectType();
            assert timeProgrammer.getWorkingDaysCommands()[i].getHour() == timeProgrammer2.getWorkingDaysCommands()[i].getHour();
            assert timeProgrammer.getWorkingDaysCommands()[i].getMinute() == timeProgrammer2.getWorkingDaysCommands()[i].getMinute();
        }

        for (int i = 0; i < timeProgrammer.getPreHolidayDaysCommands().length; i++) {
            assert timeProgrammer.getPreHolidayDaysCommands()[i].getAction() == timeProgrammer2.getPreHolidayDaysCommands()[i].getAction();
            assert timeProgrammer.getPreHolidayDaysCommands()[i].getObject() == timeProgrammer2.getPreHolidayDaysCommands()[i].getObject();
            assert timeProgrammer.getPreHolidayDaysCommands()[i].getObjectType() == timeProgrammer2.getPreHolidayDaysCommands()[i].getObjectType() : "Ecpected " + timeProgrammer.getPreHolidayDaysCommands()[i].getObjectType() + " but found " + timeProgrammer2.getPreHolidayDaysCommands()[i].getObjectType();
            assert timeProgrammer.getPreHolidayDaysCommands()[i].getHour() == timeProgrammer2.getPreHolidayDaysCommands()[i].getHour();
            assert timeProgrammer.getPreHolidayDaysCommands()[i].getMinute() == timeProgrammer2.getPreHolidayDaysCommands()[i].getMinute();
        }

        for (int i = 0; i < timeProgrammer.getHolidayDaysCommands().length; i++) {
            assert timeProgrammer.getHolidayDaysCommands()[i].getAction() == timeProgrammer2.getHolidayDaysCommands()[i].getAction();
            assert timeProgrammer.getHolidayDaysCommands()[i].getObject() == timeProgrammer2.getHolidayDaysCommands()[i].getObject();
            assert timeProgrammer.getHolidayDaysCommands()[i].getObjectType() == timeProgrammer2.getHolidayDaysCommands()[i].getObjectType() : "Ecpected " + timeProgrammer.getHolidayDaysCommands()[i].getObjectType() + " but found " + timeProgrammer2.getHolidayDaysCommands()[i].getObjectType();
            assert timeProgrammer.getHolidayDaysCommands()[i].getHour() == timeProgrammer2.getHolidayDaysCommands()[i].getHour();
            assert timeProgrammer.getHolidayDaysCommands()[i].getMinute() == timeProgrammer2.getHolidayDaysCommands()[i].getMinute();
        }

        for (int i = 0; i < timeProgrammer.getDayClasses().length; i++) {
            assert timeProgrammer.getDayClasses()[i] == timeProgrammer2.getDayClasses()[i];
        }
    }
}
