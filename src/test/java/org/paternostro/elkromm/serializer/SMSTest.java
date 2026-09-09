package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.SMS;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMSTest {
    @Test
    public void test()
    {
        SMS     sms = new SMS("a text");
        byte[]  data = ElkrommFactory.getFactory().getSMSSerializer().serialize(sms);

        assert data.length == 40 : "Wrong length";

        SMS sms2 = ElkrommFactory.getFactory().getSMSSerializer().deserialize(data);

        assert sms.getText().equals(sms2.getText());
    }
}
