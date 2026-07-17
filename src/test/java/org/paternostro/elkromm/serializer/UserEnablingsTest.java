package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.UserEnablings;

public class UserEnablingsTest {
    @Test
    public void test()
    {
        boolean[]       enablings = { false, true, false};
        UserEnablings   userEnablings = new UserEnablings(enablings);

        byte[]  data = ElkrommFactory.getFactory().getUserEnablingsSerializer().serialize(userEnablings);

        assert data.length == 4 : "Wrong length";

        UserEnablings   userEnablings2 = ElkrommFactory.getFactory().getUserEnablingsSerializer().deserialize(data);

        assert userEnablings.getEnablings().length == userEnablings2.getEnablings().length;

        for (int i = 0; i < userEnablings.getEnablings().length; i++) {
            assert userEnablings.getEnablings()[i] == userEnablings2.getEnablings()[i] : "Expected " + userEnablings.getEnablings()[i] + " got " + userEnablings2.getEnablings()[i];
        }
    }
}
