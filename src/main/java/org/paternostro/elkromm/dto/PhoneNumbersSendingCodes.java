package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.PhoneNumber.Event;

public class PhoneNumbersSendingCodes implements Serializable {
    private PhoneNumber[]   phoneNumbers;

    public PhoneNumbersSendingCodes(PhoneNumber[] phoneNumbers) {
        setPhoneNumbers(phoneNumbers);
    }

    public PhoneNumber[] getPhoneNumbers() {
        return Arrays.copyOf(phoneNumbers, ElkrommFacade.MAX_PHONE_NUMBERS);
    }

    public void setPhoneNumbers(PhoneNumber[] phoneNumbers) {
        if (phoneNumbers == null) throw new IllegalArgumentException("Missing mandatory phone numbers");

        this.phoneNumbers = Arrays.copyOf(phoneNumbers, ElkrommFacade.MAX_PHONE_NUMBERS);
    }

    public int getAssignedEventMask(Event event) {
        int retval = 0;
        int mask = 0x01;

        for (PhoneNumber pivot : phoneNumbers) {
            retval |= pivot.isAssignedEvent(event) ? mask : 0x00;
            mask <<= 1;
        }

        return retval;
    }
}
