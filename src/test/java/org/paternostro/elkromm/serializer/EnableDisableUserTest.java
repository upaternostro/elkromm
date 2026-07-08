package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;

public class EnableDisableUserTest {
    @Test
    public void test()
    {
        org.paternostro.elkromm.dto.EnableDisableUser   edu = new org.paternostro.elkromm.dto.EnableDisableUser((byte)12, true);
        byte[]                                          data = ElkrommFactory.getFactory().getEnableDisableUserSerializer().serialize(edu);

        assert data.length == 2 : "Wrong length";

        org.paternostro.elkromm.dto.EnableDisableUser   edu2 = ElkrommFactory.getFactory().getEnableDisableUserSerializer().deserialize(data);

        assert edu.getOrdinal() == edu2.getOrdinal();
        assert edu.isEnabled() == edu2.isEnabled();
    }
}
