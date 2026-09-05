package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMSs implements ElkrommSerializer<org.paternostro.elkromm.dto.SMSs>
{
    public static final int SMS_SIZE  = 40;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SMSs obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                              data = new byte[length()];
        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();

        Arrays.fill(data, 0, data.length - 4, (byte)0xff); // Pad with 0xff bytes
        
        for (int i = 0; i < obj.getSMSs().length; i++) {
            System.arraycopy(smsSerializer.serialize(obj.getSMSs()[i]), 0, data, i * SMS_SIZE, SMS_SIZE);
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SMSs deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        org.paternostro.elkromm.dto.SMS[]                   sMSs = new org.paternostro.elkromm.dto.SMS[ElkrommFacade.MAX_SMS];
        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();
        byte[]                                              smsData = new byte[SMS_SIZE];

        for (int i = 0; i < ElkrommFacade.MAX_SMS; i++) {
            System.arraycopy(data, i * SMS_SIZE, smsData, 0, SMS_SIZE);
            sMSs[i] = smsSerializer.deserialize(smsData);
        }

        return new org.paternostro.elkromm.dto.SMSs(sMSs);
    }

    @Override
    public int length()
    {
        return 364;
    }
}
