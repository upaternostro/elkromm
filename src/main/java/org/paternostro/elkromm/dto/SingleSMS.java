package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.dto.SMSs.SMSIndex;

/**
 * A single {@link SMS} message paired with its {@link SMSIndex}, for the
 * {@code SMS_PROGRAMMING} single-instance write command.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleSMS implements Serializable {
    private SMSIndex    index;
    private SMS         sMS;

    /**
     * Creates a new single-SMS write.
     *
     * @param index which event this message is for
     * @param sMS the message text
     */
    public SingleSMS(SMSIndex index, SMS sMS) {
        setIndex(index);
        setSMS(sMS);
    }

    /**
     * Returns which event this message is for.
     *
     * @return the event index
     */
    public SMSIndex getIndex() {
        return index;
    }

    /**
     * Sets which event this message is for.
     *
     * @param index the event index to set, not {@code null}
     * @throws IllegalArgumentException if {@code index} is {@code null}
     */
    public void setIndex(SMSIndex index) {
        if (index == null) throw new IllegalArgumentException("Missing mandatory index");

        this.index = index;
    }

    /**
     * Returns the message text.
     *
     * @return the message
     */
    public SMS getSMS() {
        return sMS;
    }

    /**
     * Sets the message text.
     *
     * @param sMS the message to set, not {@code null}
     * @throws IllegalArgumentException if {@code sMS} is {@code null}
     */
    public void setSMS(SMS sMS) {
        if (sMS == null) throw new IllegalArgumentException("Missing mandatory sMS");

        this.sMS = sMS;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{index=").append(index).append(", sMS=").append(sMS).append("}");

        return sb.toString();
    }
}
