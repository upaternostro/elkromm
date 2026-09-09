package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.PhoneNumber;
import org.paternostro.elkromm.dto.PhoneNumber.Event;
import org.paternostro.elkromm.dto.PhoneNumber.SendingMode;
import org.paternostro.elkromm.dto.PhoneNumber.Type;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneNumbersSendingCodesTest {
    public static final boolean[]   emptyAssociatedPartitions = { false, false, false, false, false, false, false, false };
    public static final PhoneNumber emptyPhonenumber = new PhoneNumber("", emptyAssociatedPartitions, Type.PNT_PSTN, SendingMode.PNSM_VOICE, new Event[0]);

    @Test
    public void test()
    {
        boolean[]                                   associatedPartitions = { false, false, true, false, true, false, false, true };
        Event[]                                     assignedEvents = { };
        org.paternostro.elkromm.dto.PhoneNumber[]   phoneNumbers = new org.paternostro.elkromm.dto.PhoneNumber[ElkrommFacade.MAX_PHONE_NUMBERS];

        phoneNumbers[0] = new org.paternostro.elkromm.dto.PhoneNumber("800123456", associatedPartitions, Type.PNT_PSTN, SendingMode.PNSM_VOICE, assignedEvents);
        phoneNumbers[0].addAssignedEvent(Event.PNSCE_BURGLAR_ALARM);
        phoneNumbers[0].addAssignedEvent(Event.PNSCE_TAMPERING);

        for (int i = 1; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            phoneNumbers[i] = emptyPhonenumber;
        }

        org.paternostro.elkromm.dto.PhoneNumbersSendingCodes    phoneNumbersSendingCodes = new org.paternostro.elkromm.dto.PhoneNumbersSendingCodes(phoneNumbers);
        byte[]                                                  data = ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().serialize(phoneNumbersSendingCodes);

        assert data.length == 408 : "Wrong length";

        org.paternostro.elkromm.dto.PhoneNumbersSendingCodes phoneNumbersSendingCodes2 = ElkrommFactory.getFactory().getPhoneNumbersSendingCodesSerializer().deserialize(data);

        for (int i = 0; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            assert phoneNumbersSendingCodes.getPhoneNumbers()[i].getPhoneNumber().equals(phoneNumbersSendingCodes2.getPhoneNumbers()[i].getPhoneNumber());

            for (int j = 0; j < ElkrommFacade.MAX_PARTITIONS; j++) {
                assert phoneNumbersSendingCodes.getPhoneNumbers()[i].getAssociatedPartitions()[j] == phoneNumbersSendingCodes2.getPhoneNumbers()[i].getAssociatedPartitions()[j];
            }

            assert phoneNumbersSendingCodes.getPhoneNumbers()[i].getType() == phoneNumbersSendingCodes2.getPhoneNumbers()[i].getType();
            assert phoneNumbersSendingCodes.getPhoneNumbers()[i].getSendingMode() == phoneNumbersSendingCodes2.getPhoneNumbers()[i].getSendingMode();

            for (Event pivot : Event.values()) {
                assert phoneNumbersSendingCodes.getAssignedEventMask(pivot) == phoneNumbersSendingCodes2.getAssignedEventMask(pivot);
            }
        }
    }
}
