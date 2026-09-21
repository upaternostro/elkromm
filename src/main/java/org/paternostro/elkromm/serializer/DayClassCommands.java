package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

/**
 * {@link org.paternostro.elkromm.dto.DayClassCommands} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Day class</td><td>{@link DayClass}, identifies which of the three command sets is being written</td><td>{@link #DAY_CLASS_OFFSET}</td></tr>
 *  <tr><td>0x01-0x28</td><td>8 commands</td><td>5 bytes each, see {@link Commands} serializer</td><td>{@link #COMMANDS_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Commands
 */
public class DayClassCommands implements ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 41;

    /** Offset of the day class */
    public static final int DAY_CLASS_OFFSET = 0x00;

    /** Offset of the commands, see {@link Commands} */
    public static final int COMMANDS_OFFSET  = DAY_CLASS_OFFSET + 1;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.DayClassCommands obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[DAY_CLASS_OFFSET] = obj.getDayClass().getValue();
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getCommands()), 0, data, COMMANDS_OFFSET, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.DayClassCommands deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        byte[]  cData = new byte[ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH];

        System.arraycopy(data, COMMANDS_OFFSET, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        return new org.paternostro.elkromm.dto.DayClassCommands(DayClass.valueOf(data[DAY_CLASS_OFFSET]), ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData));
    }
}
