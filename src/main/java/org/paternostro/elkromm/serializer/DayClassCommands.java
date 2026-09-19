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
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Day class</td><td>{@link DayClass}, identifies which of the three command sets is being written</td></tr>
 *  <tr><td>1-40</td><td>8 commands</td><td>5 bytes each, see {@link Commands} serializer</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Commands
 */
public class DayClassCommands implements ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.DayClassCommands obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[0] = obj.getDayClass().getValue();
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getCommands()), 0, data, 1, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.DayClassCommands deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        byte[]  cData = new byte[ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH];

        System.arraycopy(data, 1, cData, 0, ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH);
        
        return new org.paternostro.elkromm.dto.DayClassCommands(DayClass.valueOf(data[0]), ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData));
    }

    @Override
    public int length()
    {
        return 41;
    }
}
