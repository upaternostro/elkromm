package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.User;

public class UsersTest {
    @Test
    public void test()
    {
        User[]  users = new User[ElkrommFacade.MAX_CREDENTIALS];

        for (int i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            users[i] = new User("User " + (i + 1), Credential.Enabling.DISABLED, ElkrommUtils.unpackPartitions((byte)i));
        }

        byte[]  data = ElkrommFactory.getFactory().getUsersSerializer().serialize(users);

        assert data.length == 836 : "Wrong length";

        Credential[]  users2 = ElkrommFactory.getFactory().getUsersSerializer().deserialize(data);

        assert users.length == users2.length;

        for (int i = 0; i < users.length; i++) {
            assert users[i].getName().equals(users2[i].getName()) : "Expected " + users[i].getName() + " got " + users2[i].getName();

            for (int j = 0; j < users[i].getAssociatedPartitions().length; j++) {
                assert users[i].getAssociatedPartitions()[j] == users2[i].getAssociatedPartitions()[j];
            }
        }
    }
}
