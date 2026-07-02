package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.SMS;
import org.paternostro.elkromm.dto.SMSs;

public class SMSsTest {
    @Test
    public void test()
    {
        SMS[]   sMSs = new SMS[ElkrommFacade.MAX_SMS];

        sMSs[0] = new SMS("a text");
        sMSs[1] = new SMS("another text");
        sMSs[2] = new SMS("third text");
        sMSs[3] = new SMS("#3");
        sMSs[4] = new SMS("#4");
        sMSs[5] = new SMS("#5");
        sMSs[6] = new SMS("#6");
        sMSs[7] = new SMS("#7");
        sMSs[8] = new SMS("#8");

        SMSs    SMSs = new SMSs(sMSs);
        byte[]  data = ElkrommFactory.getFactory().getSMSsSerializer().serialize(SMSs);

        assert data.length == 364 : "Wrong length";

        SMSs    SMSs2 = ElkrommFactory.getFactory().getSMSsSerializer().deserialize(data);

        // assert sms.getText().equals(sms2.getText());
        assert SMSs.getSMSs() != null;
        assert SMSs2.getSMSs() != null;
        assert SMSs.getSMSs().length == SMSs2.getSMSs().length;

        for (int i = 0; i < SMSs.getSMSs().length; i++) {
            assert SMSs.getSMSs()[i] != null;
            assert SMSs2.getSMSs()[i] != null;
            assert SMSs.getSMSs()[i].getText().equals(SMSs2.getSMSs()[i].getText());
        }
    }
}
