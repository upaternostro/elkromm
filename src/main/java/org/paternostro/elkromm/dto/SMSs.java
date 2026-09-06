package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * The full set of configured SMS notification messages, one per event type.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMSs implements Serializable {
    /** Which event an {@link SMS} message is sent for. */
    public enum SMSIndex {
        /** Burglary alarm. */
        SMS_BURLGAR,
        /** Technical alarm, first type. */
        SMS_TECHNICAL_ALARM_1,
        /** Technical alarm, second type. */
        SMS_TECHNICAL_ALARM_2,
        /** Technical alarm, third type. */
        SMS_TECHNICAL_ALARM_3,
        /** Fire alarm. */
        SMS_FIRE,
        /** A partition was armed. */
        SMS_PARTITION_ON,
        /** A partition was disarmed. */
        SMS_PARTITION_OFF,
        /** Tampering detected. */
        SMS_TAMPERING,
        /** Generic notice. */
        SMS_NOTICE;

        /**
         * Looks up the {@code SMSIndex} matching a given ordinal.
         *
         * @param ordinal the ordinal to look up
         * @return the matching index, or {@code null} if none matches
         */
        public static SMSIndex valueOf(int ordinal) {
            SMSIndex    retval = null;

            for (SMSIndex pivot : SMSIndex.values()) {
                if (ordinal == pivot.ordinal()) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private SMS[]   sMSs;

    /**
     * Creates a new SMS message set.
     *
     * @param sMSs one message per {@link SMSIndex}, in enum order
     */
    public SMSs(SMS[] sMSs) {
        this.sMSs = sMSs;
    }

    /**
     * Returns the configured messages, in {@link SMSIndex} order.
     *
     * @return the messages
     */
    public SMS[] getSMSs() {
        return sMSs;
    }

    /**
     * Sets the configured messages.
     *
     * @param sMSs one message per {@link SMSIndex}, in enum order
     */
    public void setSMSs(SMS[] sMSs) {
        this.sMSs = Arrays.copyOf(sMSs, ElkrommFacade.MAX_SMS);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{sMSs=").append(Arrays.toString(sMSs)).append("}");

        return sb.toString();
    }
}
