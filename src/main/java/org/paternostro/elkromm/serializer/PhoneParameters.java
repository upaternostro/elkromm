package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallFrequency;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallInterval;
import org.paternostro.elkromm.dto.PhoneParameters.Enabling;
import org.paternostro.elkromm.dto.PhoneParameters.ReturnCall;
import org.paternostro.elkromm.dto.PhoneParameters.VoiceMessagesSendingMode;

/**
 * {@link org.paternostro.elkromm.dto.PhoneParameters} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-4</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>5</td><td>Call delay</td><td>{@link Enabling}: 0=disabled, 1=enabled</td></tr>
 *  <tr><td>6</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>7</td><td>Return call</td><td>{@link ReturnCall}: 0=disabled, 1=type A, 2=type B</td></tr>
 *  <tr><td>8</td><td>Remote surveillance</td><td>{@link Enabling}</td></tr>
 *  <tr><td>9</td><td>Voice message sending mode</td><td>{@link VoiceMessagesSendingMode}: 0=none, 1-4=mode 1-4</td></tr>
 *  <tr><td>10</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>11</td><td>Cyclic test call frequency</td><td>{@link CyclicTestCallFrequency}: 0=disabled, 1=24h, 2=when system armed</td></tr>
 *  <tr><td>12</td><td>Phone number index for the test call</td><td>0-12 (0 if disabled)</td></tr>
 *  <tr><td>13</td><td>Test call hour</td><td>0-23</td></tr>
 *  <tr><td>14</td><td>Test call minute</td><td>0-59</td></tr>
 *  <tr><td>15</td><td>Test call interval</td><td>{@link CyclicTestCallInterval}: 0=1h, 1=4h, 2=8h, 3=12h, 4=24h, 5=48h, 6=72h, 7=96h, 8=120h, 9=144h, 10=168h</td></tr>
 *  <tr><td>16-19</td><td>Block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneParameters implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneParameters obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        data[ 0] = data[ 1] = data[ 2] = data[ 3] = 0x66;
        data[ 4] = 0x01;
        data[ 5] = obj.getCallDelay().getValue();
        data[ 6] = 0x07;
        data[ 7] = obj.getReturnCall().getValue();
        data[ 8] = obj.getRemoteSurveillance().getValue();
        data[ 9] = obj.getVoiceMessagesSendingMode().getValue();
        data[10] = 0x02;
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
        return SerializersConstants.PHONE_PARAMETERS_SIZE;
    }
}
