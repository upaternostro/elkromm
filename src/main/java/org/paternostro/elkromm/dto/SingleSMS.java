package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.dto.SMSs.SMSIndex;

public class SingleSMS implements Serializable {
    private SMSIndex    index;
    private SMS         sMS;

    public SingleSMS(SMSIndex index, SMS sMS) {
        setIndex(index);
        setSMS(sMS);
    }

    public SMSIndex getIndex() {
        return index;
    }

    public void setIndex(SMSIndex index) {
        if (index == null) throw new IllegalArgumentException("Missing mandatory index");

        this.index = index;
    }

    public SMS getSMS() {
        return sMS;
    }

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
