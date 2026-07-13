package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Command.Action;
import org.paternostro.elkromm.dto.Command.ObjectType;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;

public class DayClassCommandsTest {
    @Test
    public void test()
    {
        Command[]   commands = new Command[8];

        for (int i = 0; i < commands.length; i++) {
            commands[i] = new Command(i % 2 == 0 ? Action.CA_ENABLE : Action.CA_DISABLE, (byte)(i + 1) , i % 2 == 0 ? ObjectType.COT_SECTORS : ObjectType.COT_USER, (byte)(23 - i), (byte)(59 - i));
        }

        org.paternostro.elkromm.dto.DayClassCommands    dayClassCommands = new org.paternostro.elkromm.dto.DayClassCommands(DayClass.DCCDC_HOLIDAY, commands);

        byte[]  data = ElkrommFactory.getFactory().getDayClassCommandsSerializer().serialize(dayClassCommands);

        assert data.length == 41 : "Wrong length";

        org.paternostro.elkromm.dto.DayClassCommands   dayClassCommands2 = ElkrommFactory.getFactory().getDayClassCommandsSerializer().deserialize(data);

        assert dayClassCommands.getDayClass() == dayClassCommands2.getDayClass();
        assert dayClassCommands.getCommands().length == dayClassCommands2.getCommands().length;

        for (int i = 0; i < dayClassCommands.getCommands().length; i++) {
            assert dayClassCommands.getCommands()[i].getAction() == dayClassCommands2.getCommands()[i].getAction();
            assert dayClassCommands.getCommands()[i].getObject() == dayClassCommands2.getCommands()[i].getObject();
            assert dayClassCommands.getCommands()[i].getObjectType() == dayClassCommands2.getCommands()[i].getObjectType() : "Ecpected " + dayClassCommands.getCommands()[i].getObjectType() + " but found " + dayClassCommands2.getCommands()[i].getObjectType();
            assert dayClassCommands.getCommands()[i].getHour() == dayClassCommands2.getCommands()[i].getHour();
            assert dayClassCommands.getCommands()[i].getMinute() == dayClassCommands2.getCommands()[i].getMinute();
        }
    }
}
