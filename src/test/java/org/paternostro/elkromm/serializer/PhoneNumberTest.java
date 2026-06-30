package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.PhoneNumber.Event;
import org.paternostro.elkromm.dto.PhoneNumber.SendingMode;
import org.paternostro.elkromm.dto.PhoneNumber.Type;

public class PhoneNumberTest {
    @Test
    public void test()
    {
        boolean[]                               associatedPartitions = { false, false, true, false, true, false, false, true };
        Event[]                                 assignedEvents = { /* Event.PNSCE_BURGLAR_ALARM, Event.PNSCE_TAMPERING */ };
        org.paternostro.elkromm.dto.PhoneNumber phoneNumber = new org.paternostro.elkromm.dto.PhoneNumber("800123456", associatedPartitions, Type.PNT_PSTN, SendingMode.PNSM_VOICE, assignedEvents);
        byte[]                                  data = ElkrommFactory.getFactory().getPhoneNumberSerializer().serialize(phoneNumber);

        assert data.length == 17 : "Wrong length";

        org.paternostro.elkromm.dto.PhoneNumber phoneNumber2 = ElkrommFactory.getFactory().getPhoneNumberSerializer().deserialize(data);

        assert phoneNumber.getPhoneNumber().equals(phoneNumber2.getPhoneNumber());

        for (int i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            assert phoneNumber.getAssociatedPartitions()[i] == phoneNumber2.getAssociatedPartitions()[i];
        }

        assert phoneNumber.getType() == phoneNumber2.getType();
        assert phoneNumber.getSendingMode() == phoneNumber2.getSendingMode();
    }
}
