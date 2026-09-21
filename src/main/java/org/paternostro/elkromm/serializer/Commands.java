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
 *  <tr><th>Offset (relative to the command)</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Action</td><td>{@link Command.Action}: 0=none, 1=enable, 2=disable</td><td></td></tr>
 *  <tr><td>0x01</td><td>Object</td><td>Object index (partition or user, depending on the next byte)</td><td>{@link #OBJECT_OFFSET}</td></tr>
 *  <tr><td>0x02</td><td>Object type</td><td>{@link Command.ObjectType}: {@code 0x10}=partitions, {@code 0x40}=user — a code comment notes "more to come: keys, outputs", so the list may not be complete</td><td>{@link #OBJECT_TYPE_OFFSET}</td></tr>
 *  <tr><td>0x03</td><td>Hour</td><td></td><td>{@link #HOUR_OFFSET}</td></tr>
 *  <tr><td>0x04</td><td>Minute</td><td></td><td>{@link #MINUTE_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link DayClassCommands}
 * @usedby {@link TimeProgrammer}
 */
public class Commands implements ElkrommSerializer<Command[]>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = ElkrommFacade.NUM_COMMANDS * SerializersConstants.COMMAND_LENGTH;

    /** Offset of the object index (partition or user, depending on the object type), relative to the command */
    public static final int OBJECT_OFFSET      = 0x01;

    /** Offset of the object type, relative to the command */
    public static final int OBJECT_TYPE_OFFSET = 0x02;

    /** Offset of the hour, relative to the command */
    public static final int HOUR_OFFSET        = 0x03;

    /** Offset of the minute, relative to the command */
    public static final int MINUTE_OFFSET      = 0x04;

    @Override
    public byte[] serialize(Command[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length != ElkrommFacade.NUM_COMMANDS) throw new IllegalArgumentException("Wrong obj length");

        byte[]  data = new byte[PAYLOAD_SIZE];
        int     offset;

        for (int i = 0; i < ElkrommFacade.NUM_COMMANDS; i++) {
            offset = i * SerializersConstants.COMMAND_LENGTH;
            data[offset] = obj[i].getAction().getValue();

            if (obj[i].getAction() != Action.CA_NONE) {
                data[offset + OBJECT_OFFSET] = obj[i].getObject();
                data[offset + OBJECT_TYPE_OFFSET] = obj[i].getObjectType().getValue();
                data[offset + HOUR_OFFSET] = obj[i].getHour();
                data[offset + MINUTE_OFFSET] = obj[i].getMinute();
            }
        }

        return data;
    }

    @Override
    public Command[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        Command[]   retval = new Command[ElkrommFacade.NUM_COMMANDS];
        int         offset;

        for (int i = 0; i < ElkrommFacade.NUM_COMMANDS; i++) {
            offset = i * SerializersConstants.COMMAND_LENGTH;
            retval[i] = new Command(Action.valueOf(data[offset]), data[offset + OBJECT_OFFSET] , ObjectType.valueOf(data[offset + OBJECT_TYPE_OFFSET]), data[offset + HOUR_OFFSET], data[offset + MINUTE_OFFSET]);
        }

        return retval;
    }
}
