package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Key;

public class KeyTest {
    @Test
    public void test()
    {
        Key     key = new Key("Key 1" , Credential.Enabling.DISABLED, Key.Specialization.KS_CHANGE_PARTITION_STATUS, ElkrommUtils.unpackPartitions((byte)1));
        byte[]  data = ElkrommFactory.getFactory().getKeySerializer().serialize(key);

        assert data.length == 26 : "Wrong length";

        Credential  keys2 = ElkrommFactory.getFactory().getKeySerializer().deserialize(data);

        assert key.getName().equals(keys2.getName()) : "Expected " + key.getName() + " got " + keys2.getName();

        for (int i = 0; i < key.getAssociatedPartitions().length; i++) {
            assert key.getAssociatedPartitions()[i] == keys2.getAssociatedPartitions()[i];
        }
    }
}
