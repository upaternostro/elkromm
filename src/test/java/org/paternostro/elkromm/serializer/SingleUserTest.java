package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential.Enabling;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleUserTest {
    @Test
    public void test()
    {
        org.paternostro.elkromm.dto.SingleCredential    singleUser = new org.paternostro.elkromm.dto.SingleCredential((byte)12, new org.paternostro.elkromm.dto.User("User 17", Enabling.DISABLED, ElkrommUtils.unpackPartitions((byte)18)));
        byte[]                                          data = ElkrommFactory.getFactory().getSingleUserSerializer().serialize(singleUser);

        assert data.length == 27 : "Wrong length";

        org.paternostro.elkromm.dto.SingleCredential    singleUser2 = ElkrommFactory.getFactory().getSingleUserSerializer().deserialize(data);

        assert singleUser.getIndex() == singleUser2.getIndex() : "Expected " + singleUser.getIndex() + " got " + singleUser2.getIndex();
        assert singleUser.getCredential().getName().equals(singleUser2.getCredential().getName()) : "Expected " + singleUser.getCredential().getName() + " got " + singleUser2.getCredential().getName();

        for (int i = 0; i < singleUser.getCredential().getAssociatedPartitions().length; i++) {
            assert singleUser.getCredential().getAssociatedPartitions()[i] == singleUser2.getCredential().getAssociatedPartitions()[i];
        }
    }
}
