package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Key;

public class KeysTest {
    @Test
    public void test()
    {
        Key[]  keys = new Key[ElkrommFacade.MAX_CREDENTIALS];

        for (int i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            keys[i] = new Key("Key " + (i + 1), Credential.Enabling.DISABLED, Key.Specialization.KS_CHANGE_PARTITION_STATUS, ElkrommUtils.unpackPartitions((byte)i));
        }

        byte[]  data = ElkrommFactory.getFactory().getKeysSerializer().serialize(keys);

        assert data.length == 836 : "Wrong length";

        Credential[]  keys2 = ElkrommFactory.getFactory().getKeysSerializer().deserialize(data);

        assert keys.length == keys2.length;

        for (int i = 0; i < keys.length; i++) {
            assert keys[i].getName().equals(keys2[i].getName()) : "Expected " + keys[i].getName() + " got " + keys2[i].getName();

            for (int j = 0; j < keys[i].getAssociatedPartitions().length; j++) {
                assert keys[i].getAssociatedPartitions()[j] == keys2[i].getAssociatedPartitions()[j];
            }
        }
    }
}
