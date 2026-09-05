package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.PhoneNumber.Event;

/**
 * The panel's configured phone numbers together with which events each one
 * is set up to report.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneNumbersSendingCodes implements Serializable {
    private PhoneNumber[]   phoneNumbers;

    /**
     * Creates a new phone numbers/sending codes snapshot.
     *
     * @param phoneNumbers all configured phone numbers, length {@link ElkrommFacade#MAX_PHONE_NUMBERS}
     */
    public PhoneNumbersSendingCodes(PhoneNumber[] phoneNumbers) {
        setPhoneNumbers(phoneNumbers);
    }

    /**
     * Returns a copy of the configured phone numbers.
     *
     * @return the phone numbers, length {@link ElkrommFacade#MAX_PHONE_NUMBERS}
     */
    public PhoneNumber[] getPhoneNumbers() {
        return Arrays.copyOf(phoneNumbers, ElkrommFacade.MAX_PHONE_NUMBERS);
    }

    /**
     * Sets the configured phone numbers.
     *
     * @param phoneNumbers the phone numbers to set, not {@code null}
     * @throws IllegalArgumentException if {@code phoneNumbers} is {@code null}
     */
    public void setPhoneNumbers(PhoneNumber[] phoneNumbers) {
        if (phoneNumbers == null) throw new IllegalArgumentException("Missing mandatory phone numbers");

        this.phoneNumbers = Arrays.copyOf(phoneNumbers, ElkrommFacade.MAX_PHONE_NUMBERS);
    }

    /**
     * Computes a bitmask of which phone numbers are assigned to report a given event.
     *
     * @param event the event to check
     * @return a bitmask, one bit per phone number (LSB = first number), set if that number reports {@code event}
     */
    public int getAssignedEventMask(Event event) {
        int retval = 0;
        int mask = 0x01;

        for (PhoneNumber pivot : phoneNumbers) {
            retval |= pivot.isAssignedEvent(event) ? mask : 0x00;
            mask <<= 1;
        }

        return retval;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{phoneNumbers=").append(Arrays.toString(phoneNumbers)).append("}");

        return sb.toString();
    }
}
