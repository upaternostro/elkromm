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
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Enable PSTN network</td><td>{@link Enabling}</td></tr>
 *  <tr><td>1</td><td>Country</td><td>{@link Country}: 0=Italy, 1=France, 2=Germany, 3=Czech Rep., 4=Poland, 5=Spain, 6=Portugal, 7=Greece, 8=England</td></tr>
 *  <tr><td>2-3</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>4</td><td>PABX local access digit</td><td>{@link PABXLocalAccessDigit}: 0-9, {@code 0xff}=disabled</td></tr>
 *  <tr><td>5</td><td>Tone control</td><td>{@link Enabling}</td></tr>
 *  <tr><td>6</td><td>Answer control</td><td>{@link Enabling}</td></tr>
 *  <tr><td>7</td><td>PSTN line test</td><td>{@link PSTNLineTestFrequency}: 0=disabled, 1=24h, 2=when system armed</td></tr>
 *  <tr><td>8</td><td>PSTN answering machine rings</td><td>{@link PSTNAnsweringMachineRings}: 0=disabled, 2/4/8=number of rings</td></tr>
 *  <tr><td>9</td><td>Enable GSM network</td><td>{@link Enabling}</td></tr>
 *  <tr><td>10</td><td>GSM answering machine (no ring)</td><td>{@link Enabling}</td></tr>
 *  <tr><td>11</td><td>Incoming SMS</td><td>{@link Enabling}</td></tr>
 *  <tr><td>12-14</td><td>GSM PIN</td><td>BCD, 3 bytes; {@code 0xff 0xff 0xff} = no PIN set</td></tr>
 *  <tr><td>15</td><td>Expiration month</td></tr>
 *  <tr><td>16</td><td>Expiration year</td></tr>
 *  <tr><td>17-20</td><td>Block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PSTNGSM implements ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM>
{
    public static final int PAYLOAD_SIZE = 21;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PSTNGSM obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[ 0] = obj.getEnablePSTN().getValue();
        data[ 1] = obj.getCountry().getValue();
        data[ 4] = obj.getPABXLocalAccessDigit().getValue();
        data[ 5] = obj.getToneControl().getValue();
        data[ 6] = obj.getAnswerControl().getValue();
        data[ 7] = obj.getPSTNLineTestFrequency().getValue();
        data[ 8] = obj.getPSTNAnsweringMachineRings().getValue();
        data[ 9] = obj.getEnableGSM().getValue();
        data[10] = obj.getEnableGSMAnsweringMachine().getValue();
        data[11] = obj.getEnableIncomingSMS().getValue();
        
        if (obj.getGSMPin() == -1) {
            data[12] = data[13] = data[14] = (byte)0xff;
        } else {
            int offset = 12;

            for (byte pivot : ElkrommUtils.bcd(obj.getGSMPin(), 3)) {
                data[offset++] = pivot;
            }
        }

        data[15] = obj.getExpirationMonth();
        data[16] = obj.getExpirationYear();

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PSTNGSM deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        return new org.paternostro.elkromm.dto.PSTNGSM(Enabling.valueOf(data[0]), Country.valueOf(data[1]), PABXLocalAccessDigit.valueOf(data[4]), Enabling.valueOf(data[5]), Enabling.valueOf(data[6]), PSTNLineTestFrequency.valueOf(data[7]), PSTNAnsweringMachineRings.valueOf(data[8]), Enabling.valueOf(data[9]), Enabling.valueOf(data[10]), Enabling.valueOf(data[11]), data[12] == (byte)0xff && data[13] == (byte)0xff && data[14] == (byte)0xff ? -1 : ElkrommUtils.dcb(data, 12, 3), data[15], data[16]);
    }
}
