package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

public class TimeProgrammer implements ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer>
{
    public final static int COMMAND_LENGTH  = 5;
    public final static int NUM_COMMANDS    = 8;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.TimeProgrammer obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];
        int     offset = 3 * NUM_COMMANDS * COMMAND_LENGTH;

        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getWorkingDaysCommands()), 0, data, 0, NUM_COMMANDS * COMMAND_LENGTH);
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getPreHolidayDaysCommands()), 0, data, NUM_COMMANDS * COMMAND_LENGTH, NUM_COMMANDS * COMMAND_LENGTH);
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getHolidayDaysCommands()), 0, data, 2 * NUM_COMMANDS * COMMAND_LENGTH, NUM_COMMANDS * COMMAND_LENGTH);

        for (DayClass pivot : obj.getDayClasses()) {
            data[offset++] = pivot.getValue();
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.TimeProgrammer deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        byte[]  cData = new byte[NUM_COMMANDS * COMMAND_LENGTH];

        System.arraycopy(data, 0, cData, 0, NUM_COMMANDS * COMMAND_LENGTH);
        
        Command[]  workingDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);

        System.arraycopy(data, NUM_COMMANDS * COMMAND_LENGTH, cData, 0, NUM_COMMANDS * COMMAND_LENGTH);
        
        Command[]  preHolidayDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);

        System.arraycopy(data, 2 * NUM_COMMANDS * COMMAND_LENGTH, cData, 0, NUM_COMMANDS * COMMAND_LENGTH);
        
        Command[]  holidayDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);
        DayClass[]  dayClasses = new DayClass[7];
        int         offset = 3 * NUM_COMMANDS * COMMAND_LENGTH;

        for (int i = 0; i < 7; i++) {
            dayClasses[i] = DayClass.valueOf(data[i + offset]);
        }

        return new org.paternostro.elkromm.dto.TimeProgrammer(workingDayCommands, preHolidayDayCommands, holidayDayCommands, dayClasses);
    }

    @Override
    public int length()
    {
        return 131;
    }
}
