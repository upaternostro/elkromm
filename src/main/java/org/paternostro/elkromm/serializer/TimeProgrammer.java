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
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-39</td><td>Working day commands</td><td>8 commands × 5 bytes, see {@link Commands}</td></tr>
 *  <tr><td>40-79</td><td>Pre-holiday commands</td><td>8 commands × 5 bytes</td></tr>
 *  <tr><td>80-119</td><td>Holiday commands</td><td>8 commands × 5 bytes</td></tr>
 *  <tr><td>120</td><td>Day class — Monday</td><td>{@link DayClass}: 0=working day, 1=pre-holiday, 2=holiday</td></tr>
 *  <tr><td>121</td><td>Day class — Tuesday</td></tr>
 *  <tr><td>122</td><td>Day class — Wednesday</td></tr>
 *  <tr><td>123</td><td>Day class — Thursday</td></tr>
 *  <tr><td>124</td><td>Day class — Friday</td></tr>
 *  <tr><td>125</td><td>Day class — Saturday</td></tr>
 *  <tr><td>126</td><td>Day class — Sunday</td></tr>
 *  <tr><td>127-130</td><td>Block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Commands
 */
public class TimeProgrammer implements ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer>
{
    public static final int PAYLOAD_SIZE = 3 * Commands.PAYLOAD_SIZE + 7 + ElkrommUtils.CHECKSUM_SIZE;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.TimeProgrammer obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];
        int     offset = 3 * ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;

        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getWorkingDaysCommands()), 0, data, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getPreHolidayDaysCommands()), 0, data, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getHolidayDaysCommands()), 0, data, 2 * ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);

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

        System.arraycopy(data, 0, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        Command[]  workingDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);

        System.arraycopy(data, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        Command[]  preHolidayDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);

        System.arraycopy(data, 2 * ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        Command[]  holidayDayCommands = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData);
        DayClass[]  dayClasses = new DayClass[7];
        int         offset = 3 * ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;

        for (int i = 0; i < 7; i++) {
            dayClasses[i] = DayClass.valueOf(data[i + offset]);
        }

        return new org.paternostro.elkromm.dto.TimeProgrammer(workingDayCommands, preHolidayDayCommands, holidayDayCommands, dayClasses);
    }
}
