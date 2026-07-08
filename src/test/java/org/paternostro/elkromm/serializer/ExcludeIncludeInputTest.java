package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;

public class ExcludeIncludeInputTest {
    @Test
    public void test()
    {
        org.paternostro.elkromm.dto.ExcludeIncludeInput eii = new org.paternostro.elkromm.dto.ExcludeIncludeInput((byte)34, false);
        byte[]                                          data = ElkrommFactory.getFactory().getExcludeIncludeInputSerializer().serialize(eii);

        assert data.length == 2 : "Wrong length";

        org.paternostro.elkromm.dto.ExcludeIncludeInput eii2 = ElkrommFactory.getFactory().getExcludeIncludeInputSerializer().deserialize(data);

        assert eii.getOrdinal() == eii2.getOrdinal();
        assert eii.isEnabled() == eii2.isEnabled();
    }
}
