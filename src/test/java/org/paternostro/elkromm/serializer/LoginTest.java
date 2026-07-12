package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Login;

public class LoginTest {
    @Test
    public void test()
    {
        Login       login = new Login(12345678, 987654);
        byte[]      data = ElkrommFactory.getFactory().getLoginSerializer().serialize(login);

        assert data.length == 7 : "Wrong length";

        Login       login2 = ElkrommFactory.getFactory().getLoginSerializer().deserialize(data);

        assert login.getPlantCode() == login2.getPlantCode();
        assert login.getTechnicalCode() == login2.getTechnicalCode();
    }
}
