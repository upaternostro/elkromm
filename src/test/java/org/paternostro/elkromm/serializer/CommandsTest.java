package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Command.Action;
import org.paternostro.elkromm.dto.Command.ObjectType;

public class CommandsTest {
    @Test
    public void test()
    {
        Command[]   commands = new Command[8];

        for (int i = 0; i < commands.length; i++) {
            commands[i] = new Command(i % 2 == 0 ? Action.CA_ENABLE : Action.CA_DISABLE, (byte)i , i % 2 == 0 ? ObjectType.COT_SECTORS : ObjectType.COT_USER, (byte)(23 - i), (byte)(59 - i));
        }

        byte[]  data = ElkrommFactory.getFactory().getCommandsSerializer().serialize(commands);

        assert data.length == 40 : "Wrong length";

        Command[]   commands2 = ElkrommFactory.getFactory().getCommandsSerializer().deserialize(data);

        assert commands.length == commands2.length;

        for (int i = 0; i < commands.length; i++) {
            assert commands[i].getAction() == commands2[i].getAction();
            assert commands[i].getObject() == commands2[i].getObject();
            assert commands[i].getObjectType() == commands2[i].getObjectType() : "Ecpected " + commands[i].getObjectType() + " but found " + commands2[i].getObjectType();
            assert commands[i].getHour() == commands2[i].getHour();
            assert commands[i].getMinute() == commands2[i].getMinute();
        }
    }
}
