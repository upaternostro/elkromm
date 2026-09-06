package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class DayClassCommands implements ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands>
{
    public final static int COMMAND_LENGTH  = 5;
    
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.DayClassCommands obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[0] = obj.getDayClass().getValue();
        System.arraycopy(ElkrommFactory.getFactory().getCommandsSerializer().serialize(obj.getCommands()), 0, data, 1, ElkrommFacade.NUM_COMMANDS * COMMAND_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.DayClassCommands deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        byte[]  cData = new byte[ElkrommFacade.NUM_COMMANDS * COMMAND_LENGTH];

        System.arraycopy(data, 1, cData, 0, ElkrommFacade.NUM_COMMANDS * COMMAND_LENGTH);
        
        return new org.paternostro.elkromm.dto.DayClassCommands(DayClass.valueOf(data[0]), ElkrommFactory.getFactory().getCommandsSerializer().deserialize(cData));
    }

    @Override
    public int length()
    {
        return 41;
    }
}
