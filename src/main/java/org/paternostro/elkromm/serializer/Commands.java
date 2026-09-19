package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Command.Action;
import org.paternostro.elkromm.dto.Command.ObjectType;

/**
 * {@link Command}s serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: {@link ElkrommFacade#NUM_COMMANDS} instances of {@link Command},
 * each containing:
 * <p>
 * <table>
 *  <tr><th>Offset (relative to the command)</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Action</td><td>{@link Command.Action}: 0=none, 1=enable, 2=disable</td></tr>
 *  <tr><td>1</td><td>Object</td><td>Object index (partition or user, depending on the next byte)</td></tr>
 *  <tr><td>2</td><td>Object type</td><td>{@link Command.ObjectType}: {@code 0x10}=partitions, {@code 0x40}=user — a code comment notes "more to come: keys, outputs", so the list may not be complete</td></tr>
 *  <tr><td>3</td><td>Hour</td></tr>
 *  <tr><td>4</td><td>Minute</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link DayClassCommands}
 * @usedby {@link TimeProgrammer}
 */
public class Commands implements ElkrommSerializer<Command[]>
{
    @Override
    public byte[] serialize(Command[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length != ElkrommFacade.NUM_COMMANDS) throw new IllegalArgumentException("Wrong obj length");

        byte[]  data = new byte[length()];
        int     offset;

        for (int i = 0; i < ElkrommFacade.NUM_COMMANDS; i++) {
            offset = i * SerializersConstants.COMMAND_LENGTH;
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

        Command[]   retval = new Command[ElkrommFacade.NUM_COMMANDS];
        int         offset;

        for (int i = 0; i < ElkrommFacade.NUM_COMMANDS; i++) {
            offset = i * SerializersConstants.COMMAND_LENGTH;
            retval[i] = new Command(Action.valueOf(data[offset]), data[offset + 1] , ObjectType.valueOf(data[offset + 2]), data[offset + 3], data[offset + 4]);
        }

        return retval;
    }

    @Override
    public int length()
    {
        return ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;
    }
}
