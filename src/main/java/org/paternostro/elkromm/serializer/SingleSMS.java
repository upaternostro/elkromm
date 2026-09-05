package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.SMSs.SMSIndex;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleSMS implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleSMS>
{
    public static final int SMS_SIZE  = 40;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleSMS obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                              data = new byte[length()];
        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();

        data[0] = (byte)(obj.getIndex().ordinal() + 1);
        System.arraycopy(smsSerializer.serialize(obj.getSMS()), 0, data, 1, SMS_SIZE);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SingleSMS deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();
        byte[]                                              smsData = new byte[SMS_SIZE];

        System.arraycopy(data, 1, smsData, 0, SMS_SIZE);
        org.paternostro.elkromm.dto.SMS sMSs = smsSerializer.deserialize(smsData);

        return new org.paternostro.elkromm.dto.SingleSMS(SMSIndex.valueOf(data[0] - 1), sMSs);
    }

    @Override
    public int length()
    {
        return SMS_SIZE + 1;
    }
}
