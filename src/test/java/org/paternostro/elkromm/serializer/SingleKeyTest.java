package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential.Enabling;
import org.paternostro.elkromm.dto.Key.Specialization;

public class SingleKeyTest {
    @Test
    public void test()
    {
        org.paternostro.elkromm.dto.SingleCredential    singleKey = new org.paternostro.elkromm.dto.SingleCredential((byte)12, new org.paternostro.elkromm.dto.Key(12, "Key 12" , Enabling.DISABLED, Specialization.KS_CHANGE_PARTITION_STATUS, ElkrommUtils.unpackPartitions((byte)13)));
        byte[]                                          data = ElkrommFactory.getFactory().getSingleKeySerializer().serialize(singleKey);

        assert data.length == 27 : "Wrong length";

        org.paternostro.elkromm.dto.SingleCredential    singleKey2 = ElkrommFactory.getFactory().getSingleKeySerializer().deserialize(data);

        assert singleKey.getIndex() == singleKey2.getIndex() : "Expected " + singleKey.getIndex() + " got " + singleKey2.getIndex();
        assert singleKey.getCredential().getName().equals(singleKey2.getCredential().getName()) : "Expected " + singleKey.getCredential().getName() + " got " + singleKey2.getCredential().getName();

        for (int i = 0; i < singleKey.getCredential().getAssociatedPartitions().length; i++) {
            assert singleKey.getCredential().getAssociatedPartitions()[i] == singleKey2.getCredential().getAssociatedPartitions()[i];
        }
    }
}
