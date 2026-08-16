package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class SMSs implements Serializable {
    public enum SMSIndex {
        SMS_BURLGAR,
        SMS_TECHNICAL_ALARM_1,
        SMS_TECHNICAL_ALARM_2,
        SMS_TECHNICAL_ALARM_3,
        SMS_FIRE,
        SMS_PARTITION_ON,
        SMS_PARTITION_OFF,
        SMS_TAMPERING,
        SMS_NOTICE;

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

    public SMSs(SMS[] sMSs) {
        this.sMSs = sMSs;
    }

    public SMS[] getSMSs() {
        return sMSs;
    }

    public void setSMSs(SMS[] sMSs) {
        this.sMSs = Arrays.copyOf(sMSs, ElkrommFacade.MAX_SMS);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{sMSs=").append(Arrays.toString(sMSs)).append("}");

        return sb.toString();
    }
}
