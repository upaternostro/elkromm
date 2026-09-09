package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.User;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class UserTest {
    @Test
    public void test()
    {
        User    user = new User("User 1", Credential.Enabling.DISABLED, ElkrommUtils.unpackPartitions((byte)1));
        byte[]  data = ElkrommFactory.getFactory().getUserSerializer().serialize(user);

        assert data.length == 26 : "Wrong length";

        Credential  user2 = ElkrommFactory.getFactory().getUserSerializer().deserialize(data);

        assert user.getName().equals(user2.getName()) : "Expected " + user.getName() + " got " + user2.getName();

        for (int i = 0; i < user.getAssociatedPartitions().length; i++) {
            assert user.getAssociatedPartitions()[i] == user2.getAssociatedPartitions()[i];
        }
    }
}
