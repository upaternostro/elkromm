package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.SMS;
import org.paternostro.elkromm.dto.SingleSMS;
import org.paternostro.elkromm.dto.SMSs.SMSIndex;

public class SingleSMSTest {
    @Test
    public void test()
    {
        SMS         sms = new SMS("a text");
        SingleSMS   singleSMS = new SingleSMS(SMSIndex.SMS_TAMPERING, sms);
        byte[]      data = ElkrommFactory.getFactory().getSingleSMSSerializer().serialize(singleSMS);

        assert data.length == 41 : "Wrong length";

        SingleSMS   singleSMS2 = ElkrommFactory.getFactory().getSingleSMSSerializer().deserialize(data);

        assert singleSMS.getIndex() == singleSMS2.getIndex();
        assert singleSMS.getSMS().getText().equals(singleSMS2.getSMS().getText());
    }
}
