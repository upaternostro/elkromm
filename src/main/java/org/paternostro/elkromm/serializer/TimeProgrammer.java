package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

/**
 * {@link org.paternostro.elkromm.dto.TimeProgrammer} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x27</td><td>Working day commands</td><td>8 commands × 5 bytes, see {@link Commands}</td><td>{@link #WORKING_DAYS_COMMANDS_OFFSET}</td></tr>
 *  <tr><td>0x28-0x4f</td><td>Pre-holiday commands</td><td>8 commands × 5 bytes</td><td>{@link #PRE_HOLIDAY_DAYS_COMMANDS_OFFSET}</td></tr>
 *  <tr><td>0x50-0x77</td><td>Holiday commands</td><td>8 commands × 5 bytes</td><td>{@link #HOLIDAY_DAYS_COMMANDS_OFFSET}</td></tr>
 *  <tr><td>0x78</td><td>Day class — Monday</td><td>{@link DayClass}: 0=working day, 1=pre-holiday, 2=holiday</td><td>{@link #DAY_CLASSES_OFFSET}</td></tr>
 *  <tr><td>0x79</td><td>Day class — Tuesday</td><td></td><td></td></tr>
 *  <tr><td>0x7a</td><td>Day class — Wednesday</td><td></td><td></td></tr>
 *  <tr><td>0x7b</td><td>Day class — Thursday</td><td></td><td></td></tr>
 *  <tr><td>0x7c</td><td>Day class — Friday</td><td></td><td></td></tr>
 *  <tr><td>0x7d</td><td>Day class — Saturday</td><td></td><td></td></tr>
 *  <tr><td>0x7e</td><td>Day class — Sunday</td><td></td><td></td></tr>
 *  <tr><td>0x7f-0x82</td><td>Block checksum</td><td></td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Commands
 */
public class TimeProgrammer implements ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 3 * Commands.PAYLOAD_SIZE + 7 + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the working day commands, see {@link Commands} */
    public static final int WORKING_DAYS_COMMANDS_OFFSET     = 0x00;

    /** Offset of the pre-holiday commands */
    public static final int PRE_HOLIDAY_DAYS_COMMANDS_OFFSET = WORKING_DAYS_COMMANDS_OFFSET + ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;

    /** Offset of the holiday commands */
    public static final int HOLIDAY_DAYS_COMMANDS_OFFSET     = PRE_HOLIDAY_DAYS_COMMANDS_OFFSET + ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;

    /** Offset of the day classes, one byte per weekday, starting from Monday */
    public static final int DAY_CLASSES_OFFSET               = HOLIDAY_DAYS_COMMANDS_OFFSET + ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.TimeProgrammer obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];
        int     offset = DAY_CLASSES_OFFSET;

        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getWorkingDaysCommands()), 0, data, WORKING_DAYS_COMMANDS_OFFSET, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getPreHolidayDaysCommands()), 0, data, PRE_HOLIDAY_DAYS_COMMANDS_OFFSET, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getHolidayDaysCommands()), 0, data, HOLIDAY_DAYS_COMMANDS_OFFSET, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);

        for (DayClass pivot : obj.getDayClasses()) {
            data[offset++] = pivot.getValue();
        }

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.TimeProgrammer deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        byte[]  cData = new byte[ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH];

        System.arraycopy(data, WORKING_DAYS_COMMANDS_OFFSET, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        Command[]  workingDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);

        System.arraycopy(data, PRE_HOLIDAY_DAYS_COMMANDS_OFFSET, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        Command[]  preHolidayDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);

        System.arraycopy(data, HOLIDAY_DAYS_COMMANDS_OFFSET, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        Command[]  holidayDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);
        DayClass[]  dayClasses = new DayClass[7];
        int         offset = DAY_CLASSES_OFFSET;

        for (int i = 0; i < 7; i++) {
            dayClasses[i] = DayClass.valueOf(data[i + offset]);
        }

        return new org.paternostro.elkromm.dto.TimeProgrammer(workingDayCommands, preHolidayDayCommands, holidayDayCommands, dayClasses);
    }
}
