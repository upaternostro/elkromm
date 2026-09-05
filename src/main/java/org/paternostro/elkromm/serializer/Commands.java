package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Command.Action;
import org.paternostro.elkromm.dto.Command.ObjectType;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Commands implements ElkrommSerializer<Command[]>
{
    public final static int COMMAND_LENGTH  = 5;
    public final static int NUM_COMMANDS    = 8;

    @Override
    public byte[] serialize(Command[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length != NUM_COMMANDS) throw new IllegalArgumentException("Wrong obj length");

        byte[]  data = new byte[length()];
        int     offset;

        for (int i = 0; i < NUM_COMMANDS; i++) {
            offset = i * COMMAND_LENGTH;
            data[offset] = obj[i].getAction().getValue();

            if (obj[i].getAction() != Action.CA_NONE) {
                data[offset + 1] = obj[i].getObject();
                data[offset + 2] = obj[i].getObjectType().getValue();
                data[offset + 3] = obj[i].getHour();
                data[offset + 4] = obj[i].getMinute();
            }
        }

        return data;
    }

    @Override
    public Command[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        Command[]   retval = new Command[NUM_COMMANDS];
        int         offset;

        for (int i = 0; i < NUM_COMMANDS; i++) {
            offset = i * COMMAND_LENGTH;
            retval[i] = new Command(Action.valueOf(data[offset]), data[offset + 1] , ObjectType.valueOf(data[offset + 2]), data[offset + 3], data[offset + 4]);
        }

        return retval;
    }

    @Override
    public int length()
    {
        return 40;
    }
}
