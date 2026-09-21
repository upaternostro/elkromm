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
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x04</td><td>?</td><td>Not mapped by any DTO field</td><td>{@link #UNKNOWN_1_OFFSET}</td></tr>
 *  <tr><td>0x05</td><td>Call delay</td><td>{@link Enabling}: 0=disabled, 1=enabled</td><td>{@link #CALL_DELAY_OFFSET}</td></tr>
 *  <tr><td>0x06</td><td>?</td><td>Not mapped by any DTO field</td><td>{@link #UNKNOWN_2_OFFSET}</td></tr>
 *  <tr><td>0x07</td><td>Return call</td><td>{@link ReturnCall}: 0=disabled, 1=type A, 2=type B</td><td>{@link #RETURN_CALL_OFFSET}</td></tr>
 *  <tr><td>0x08</td><td>Remote surveillance</td><td>{@link Enabling}</td><td>{@link #REMOTE_SURVEILLANCE_OFFSET}</td></tr>
 *  <tr><td>0x09</td><td>Voice message sending mode</td><td>{@link VoiceMessagesSendingMode}: 0=none, 1-4=mode 1-4</td><td>{@link #VOICE_MESSAGES_SENDING_MODE_OFFSET}</td></tr>
 *  <tr><td>0x0a</td><td>?</td><td>Not mapped by any DTO field</td><td>{@link #UNKNOWN_3_OFFSET}</td></tr>
 *  <tr><td>0x0b</td><td>Cyclic test call frequency</td><td>{@link CyclicTestCallFrequency}: 0=disabled, 1=24h, 2=when system armed</td><td>{@link #CYCLIC_TEST_CALL_FREQUENCY_OFFSET}</td></tr>
 *  <tr><td>0x0c</td><td>Phone number index for the test call</td><td>0-12 (0 if disabled)</td><td>{@link #CYCLIC_TEST_CALL_PHONE_NUMBER_OFFSET}</td></tr>
 *  <tr><td>0x0d</td><td>Test call hour</td><td>0-23</td><td>{@link #CYCLIC_TEST_CALL_HOUR_OFFSET}</td></tr>
 *  <tr><td>0x0e</td><td>Test call minute</td><td>0-59</td><td>{@link #CYCLIC_TEST_CALL_MINUTE_OFFSET}</td></tr>
 *  <tr><td>0x0f</td><td>Test call interval</td><td>{@link CyclicTestCallInterval}: 0=1h, 1=4h, 2=8h, 3=12h, 4=24h, 5=48h, 6=72h, 7=96h, 8=120h, 9=144h, 10=168h</td><td>{@link #CYCLIC_TEST_CALL_INTERVAL_OFFSET}</td></tr>
 *  <tr><td>0x10-0x13</td><td>Block checksum</td><td></td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneParameters implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 20;

    /** Offset of five bytes of unknown meaning: the first four are always written as {@code 0x66}, the last as {@code 0x01} */
    public static final int UNKNOWN_1_OFFSET                     = 0x00;

    /** Offset of the call delay */
    public static final int CALL_DELAY_OFFSET                    = 0x05;

    /** Offset of a byte of unknown meaning, always written as {@code 0x07} */
    public static final int UNKNOWN_2_OFFSET                     = 0x06;

    /** Offset of the return call */
    public static final int RETURN_CALL_OFFSET                   = 0x07;

    /** Offset of the remote surveillance */
    public static final int REMOTE_SURVEILLANCE_OFFSET           = 0x08;

    /** Offset of the voice message sending mode */
    public static final int VOICE_MESSAGES_SENDING_MODE_OFFSET   = 0x09;

    /** Offset of a byte of unknown meaning, always written as {@code 0x02} */
    public static final int UNKNOWN_3_OFFSET                     = 0x0a;

    /** Offset of the cyclic test call frequency */
    public static final int CYCLIC_TEST_CALL_FREQUENCY_OFFSET    = 0x0b;

    /** Offset of the phone number index for the test call */
    public static final int CYCLIC_TEST_CALL_PHONE_NUMBER_OFFSET = 0x0c;

    /** Offset of the test call hour */
    public static final int CYCLIC_TEST_CALL_HOUR_OFFSET         = 0x0d;

    /** Offset of the test call minute */
    public static final int CYCLIC_TEST_CALL_MINUTE_OFFSET       = 0x0e;

    /** Offset of the test call interval */
    public static final int CYCLIC_TEST_CALL_INTERVAL_OFFSET     = 0x0f;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneParameters obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[UNKNOWN_1_OFFSET] = data[UNKNOWN_1_OFFSET + 1] = data[UNKNOWN_1_OFFSET + 2] = data[UNKNOWN_1_OFFSET + 3] = 0x66;
        data[UNKNOWN_1_OFFSET + 4] = 0x01;
        data[CALL_DELAY_OFFSET] = obj.getCallDelay().getValue();
        data[UNKNOWN_2_OFFSET] = 0x07;
        data[RETURN_CALL_OFFSET] = obj.getReturnCall().getValue();
        data[REMOTE_SURVEILLANCE_OFFSET] = obj.getRemoteSurveillance().getValue();
        data[VOICE_MESSAGES_SENDING_MODE_OFFSET] = obj.getVoiceMessagesSendingMode().getValue();
        data[UNKNOWN_3_OFFSET] = 0x02;
        data[CYCLIC_TEST_CALL_FREQUENCY_OFFSET] = obj.getCyclicTestCallFrequency().getValue();
        data[CYCLIC_TEST_CALL_PHONE_NUMBER_OFFSET] = obj.getCyclicTestCallPhoneNumber();
        data[CYCLIC_TEST_CALL_HOUR_OFFSET] = obj.getCyclicTestCallHour();
        data[CYCLIC_TEST_CALL_MINUTE_OFFSET] = obj.getCyclicTestCallMinute();
        data[CYCLIC_TEST_CALL_INTERVAL_OFFSET] = obj.getCyclicTestCallInterval().getValue();

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PhoneParameters deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        return new org.paternostro.elkromm.dto.PhoneParameters(Enabling.valueOf(data[CALL_DELAY_OFFSET]), ReturnCall.valueOf(data[RETURN_CALL_OFFSET]), Enabling.valueOf(data[REMOTE_SURVEILLANCE_OFFSET]), VoiceMessagesSendingMode.valueOf(data[VOICE_MESSAGES_SENDING_MODE_OFFSET]), CyclicTestCallFrequency.valueOf(data[CYCLIC_TEST_CALL_FREQUENCY_OFFSET]), data[CYCLIC_TEST_CALL_PHONE_NUMBER_OFFSET], data[CYCLIC_TEST_CALL_HOUR_OFFSET], data[CYCLIC_TEST_CALL_MINUTE_OFFSET], CyclicTestCallInterval.valueOf(data[CYCLIC_TEST_CALL_INTERVAL_OFFSET]));
    }
}
