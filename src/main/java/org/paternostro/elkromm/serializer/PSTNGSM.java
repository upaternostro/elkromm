package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PSTNGSM.Country;
import org.paternostro.elkromm.dto.PSTNGSM.Enabling;
import org.paternostro.elkromm.dto.PSTNGSM.PABXLocalAccessDigit;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNAnsweringMachineRings;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNLineTestFrequency;

/**
 * {@link org.paternostro.elkromm.dto.PSTNGSM} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Enable PSTN network</td><td>{@link Enabling}</td><td>{@link #ENABLE_PSTN_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Country</td><td>{@link Country}: 0=Italy, 1=France, 2=Germany, 3=Czech Rep., 4=Poland, 5=Spain, 6=Portugal, 7=Greece, 8=England</td><td>{@link #COUNTRY_OFFSET}</td></tr>
 *  <tr><td>0x02-0x03</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x04</td><td>PABX local access digit</td><td>{@link PABXLocalAccessDigit}: 0-9, {@code 0xff}=disabled</td><td>{@link #PABX_LOCAL_ACCESS_DIGIT_OFFSET}</td></tr>
 *  <tr><td>0x05</td><td>Tone control</td><td>{@link Enabling}</td><td>{@link #TONE_CONTROL_OFFSET}</td></tr>
 *  <tr><td>0x06</td><td>Answer control</td><td>{@link Enabling}</td><td>{@link #ANSWER_CONTROL_OFFSET}</td></tr>
 *  <tr><td>0x07</td><td>PSTN line test</td><td>{@link PSTNLineTestFrequency}: 0=disabled, 1=24h, 2=when system armed</td><td>{@link #PSTN_LINE_TEST_OFFSET}</td></tr>
 *  <tr><td>0x08</td><td>PSTN answering machine rings</td><td>{@link PSTNAnsweringMachineRings}: 0=disabled, 2/4/8=number of rings</td><td>{@link #PSTN_ANSWERING_MACHINE_RINGS_OFFSET}</td></tr>
 *  <tr><td>0x09</td><td>Enable GSM network</td><td>{@link Enabling}</td><td>{@link #ENABLE_GSM_OFFSET}</td></tr>
 *  <tr><td>0x0a</td><td>GSM answering machine (no ring)</td><td>{@link Enabling}</td><td>{@link #GSM_ANSWERING_MACHINE_OFFSET}</td></tr>
 *  <tr><td>0x0b</td><td>Incoming SMS</td><td>{@link Enabling}</td><td>{@link #INCOMING_SMS_OFFSET}</td></tr>
 *  <tr><td>0x0c-0x0e</td><td>GSM PIN</td><td>BCD, 3 bytes; {@code 0xff 0xff 0xff} = no PIN set</td><td>{@link #GSM_PIN_OFFSET}</td></tr>
 *  <tr><td>0x0f</td><td>Expiration month</td><td></td><td>{@link #EXPIRATION_MONTH_OFFSET}</td></tr>
 *  <tr><td>0x10</td><td>Expiration year</td><td></td><td>{@link #EXPIRATION_YEAR_OFFSET}</td></tr>
 *  <tr><td>0x11-0x14</td><td>Block checksum</td><td></td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PSTNGSM implements ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 21;

    /** Offset of the enable PSTN network */
    public static final int ENABLE_PSTN_OFFSET                  = 0x00;

    /** Offset of the country */
    public static final int COUNTRY_OFFSET                      = 0x01;

    /** Offset of the PABX local access digit */
    public static final int PABX_LOCAL_ACCESS_DIGIT_OFFSET      = 0x04;

    /** Offset of the tone control */
    public static final int TONE_CONTROL_OFFSET                 = 0x05;

    /** Offset of the answer control */
    public static final int ANSWER_CONTROL_OFFSET               = 0x06;

    /** Offset of the PSTN line test */
    public static final int PSTN_LINE_TEST_OFFSET               = 0x07;

    /** Offset of the PSTN answering machine rings */
    public static final int PSTN_ANSWERING_MACHINE_RINGS_OFFSET = 0x08;

    /** Offset of the enable GSM network */
    public static final int ENABLE_GSM_OFFSET                   = 0x09;

    /** Offset of the GSM answering machine (no ring) */
    public static final int GSM_ANSWERING_MACHINE_OFFSET        = 0x0a;

    /** Offset of the incoming SMS */
    public static final int INCOMING_SMS_OFFSET                 = 0x0b;

    /** Offset of the GSM PIN */
    public static final int GSM_PIN_OFFSET                      = 0x0c;

    /** Size of the GSM PIN, in BCD */
    public static final int GSM_PIN_SIZE                        = 3;

    /** Offset of the expiration month */
    public static final int EXPIRATION_MONTH_OFFSET             = GSM_PIN_OFFSET + GSM_PIN_SIZE;

    /** Offset of the expiration year */
    public static final int EXPIRATION_YEAR_OFFSET              = EXPIRATION_MONTH_OFFSET + 1;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PSTNGSM obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[ENABLE_PSTN_OFFSET] = obj.getEnablePSTN().getValue();
        data[COUNTRY_OFFSET] = obj.getCountry().getValue();
        data[PABX_LOCAL_ACCESS_DIGIT_OFFSET] = obj.getPABXLocalAccessDigit().getValue();
        data[TONE_CONTROL_OFFSET] = obj.getToneControl().getValue();
        data[ANSWER_CONTROL_OFFSET] = obj.getAnswerControl().getValue();
        data[PSTN_LINE_TEST_OFFSET] = obj.getPSTNLineTestFrequency().getValue();
        data[PSTN_ANSWERING_MACHINE_RINGS_OFFSET] = obj.getPSTNAnsweringMachineRings().getValue();
        data[ENABLE_GSM_OFFSET] = obj.getEnableGSM().getValue();
        data[GSM_ANSWERING_MACHINE_OFFSET] = obj.getEnableGSMAnsweringMachine().getValue();
        data[INCOMING_SMS_OFFSET] = obj.getEnableIncomingSMS().getValue();
        
        if (obj.getGSMPin() == -1) {
            data[GSM_PIN_OFFSET] = data[GSM_PIN_OFFSET + 1] = data[GSM_PIN_OFFSET + 2] = (byte)0xff;
        } else {
            int offset = GSM_PIN_OFFSET;

            for (byte pivot : ElkrommUtils.bcd(obj.getGSMPin(), GSM_PIN_SIZE)) {
                data[offset++] = pivot;
            }
        }

        data[EXPIRATION_MONTH_OFFSET] = obj.getExpirationMonth();
        data[EXPIRATION_YEAR_OFFSET] = obj.getExpirationYear();

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PSTNGSM deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        return new org.paternostro.elkromm.dto.PSTNGSM(Enabling.valueOf(data[ENABLE_PSTN_OFFSET]), Country.valueOf(data[COUNTRY_OFFSET]), PABXLocalAccessDigit.valueOf(data[PABX_LOCAL_ACCESS_DIGIT_OFFSET]), Enabling.valueOf(data[TONE_CONTROL_OFFSET]), Enabling.valueOf(data[ANSWER_CONTROL_OFFSET]), PSTNLineTestFrequency.valueOf(data[PSTN_LINE_TEST_OFFSET]), PSTNAnsweringMachineRings.valueOf(data[PSTN_ANSWERING_MACHINE_RINGS_OFFSET]), Enabling.valueOf(data[ENABLE_GSM_OFFSET]), Enabling.valueOf(data[GSM_ANSWERING_MACHINE_OFFSET]), Enabling.valueOf(data[INCOMING_SMS_OFFSET]), data[GSM_PIN_OFFSET] == (byte)0xff && data[GSM_PIN_OFFSET + 1] == (byte)0xff && data[GSM_PIN_OFFSET + 2] == (byte)0xff ? -1 : ElkrommUtils.dcb(data, GSM_PIN_OFFSET, GSM_PIN_SIZE), data[EXPIRATION_MONTH_OFFSET], data[EXPIRATION_YEAR_OFFSET]);
    }
}
