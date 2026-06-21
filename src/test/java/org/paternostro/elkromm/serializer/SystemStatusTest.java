package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.SystemStatus;

public class SystemStatusTest {
    @Test
    public void test()
    {
        SystemStatus    ss = new SystemStatus(ElkrommUtils.unpackPartitions((byte)(Math.random()*256)));

        byte[]  data = ElkrommFactory.getFactory().getSystemStatusSerializer().serialize(ss);

        assert data.length == 1 : "Wrong length";

        SystemStatus  ss2 = ElkrommFactory.getFactory().getSystemStatusSerializer().deserialize(data);

        for (int i = 0; i < ss.getActivePartitions().length; i++) {
            assert ss.getActivePartitions()[i] == ss2.getActivePartitions()[i];
        }
    }
}
