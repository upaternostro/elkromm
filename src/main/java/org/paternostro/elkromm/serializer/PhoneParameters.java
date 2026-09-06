package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallFrequency;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallInterval;
import org.paternostro.elkromm.dto.PhoneParameters.Enabling;
import org.paternostro.elkromm.dto.PhoneParameters.ReturnCall;
import org.paternostro.elkromm.dto.PhoneParameters.VoiceMessagesSendingMode;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneParameters implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneParameters obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[ 5] = obj.getCallDelay().getValue();
        data[ 7] = obj.getReturnCall().getValue();
        data[ 8] = obj.getRemoteSurveillance().getValue();
        data[ 9] = obj.getVoiceMessagesSendingMode().getValue();
        data[11] = obj.getCyclicTestCallFrequency().getValue();
        data[12] = obj.getCyclicTestCallPhoneNumber();
        data[13] = obj.getCyclicTestCallHour();
        data[14] = obj.getCyclicTestCallMinute();
        data[15] = obj.getCyclicTestCallInterval().getValue();

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PhoneParameters deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        return new org.paternostro.elkromm.dto.PhoneParameters(Enabling.valueOf(data[5]), ReturnCall.valueOf(data[7]), Enabling.valueOf(data[8]), VoiceMessagesSendingMode.valueOf(data[9]), CyclicTestCallFrequency.valueOf(data[11]), data[12], data[13], data[14], CyclicTestCallInterval.valueOf(data[15]));
    }

    @Override
    public int length()
    {
        return 20;
    }
}
