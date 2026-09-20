package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PhoneNumber.Event;
import org.paternostro.elkromm.dto.PhoneNumber.SendingMode;
import org.paternostro.elkromm.dto.PhoneNumber.Type;

/**
 * {@link org.paternostro.elkromm.dto.PhoneNumber} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-13</td><td>Phone number</td><td>Phone number encoded in BCD (14 bytes &rarr; 28 digits)</td></tr>
 *  <tr><td>14</td><td>Bitmask of associated partitions</td><td>LSB = partition 1</td></tr>
 *  <tr><td>15</td><td>Phone network</td><td>{@link Type}: 00 = PSTN, 01 = GSM, 02 = LAN</td></tr>
 *  <tr><td>16</td><td>Sending mode</td><td>{@link SendingMode}: 00 = Voice, 01 = IDP, 02 = ADF, 04 = Modem, 06 = SMS, 07 = C200b</td></tr>
 * </table>
 * <p>
 * <b>LAN/IP numbers</b>: when the "Phone network" field is {@code 0x02} (LAN), the phone number is replaced by an address in the fixed format {@code DDD.DDD.DDD.DDD:DDDDD} (each octet 
 * zero-padded to 3 digits, trailing part to 5 digits). Encoding <b>directly confirmed by the code</b> — {@link PhoneNumber#serialize} implements exactly this logic: each decimal digit in 
 * BCD, the dot {@code .} as nibble {@code 0x0B}, the colon {@code :} as nibble {@code 0x0C}, with the author's own original comment ("In generale gli IP sono 001B002B003B004C00005", i.e. 
 * "IPs are generally 001B002B003B004C00005") present both there and in {@link org.paternostro.elkromm.emulator.ClientConnection}. The padding of the last, unpaired nibble (for an odd number of 
 * digits/separators, as in the example below) is {@code 0x0F} — also in the code ({@code bcdByte | 0x0F}).
 * <p>
 * Example verified with a real capture, for the address `192.168.001.100:00080`:
 * <p>
 * <ul>
 *  <li>digit/separator sequence: {@code 1 9 2 B 1 6 8 B 0 0 1 B 1 0 0 C 0 0 0 8 0}</li>
 *  <li>resulting bytes: {@code 19 2b 16 8b 00 1b 10 0c 00 08 0f} (11 bytes, the last nibble {@code f} is padding) — matches the capture exactly</li>
 * </ul>
 * <p>
 * Consistent with the {@code // FIXME: IP addresses in phone numbers!} comment still present in the DTO's {@link org.paternostro.elkromm.dto.PhoneNumber#setPhoneNumber} setter — that comment 
 * only flags that the DTO doesn't yet explicitly validate/recognize the IP format (it accepts the string as-is), not that the encoding itself is uncertain: the encoding, on the serializer side, 
 * is complete.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link PhoneNumbersSendingCodes}
 */
public class PhoneNumber implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>
{
    public static final int PAYLOAD_SIZE = 17;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneNumber obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];
        int     index = 0;
        int     value;
        byte    bcdByte = 0;

        for (char pivot : obj.getPhoneNumber().toCharArray()) {
            if (obj.getType() == Type.PNT_LAN) {
                // In generale gli IP sono 001B002B003B004C00005 (attivo se tipo = LAN)
                switch (pivot) {
                    case '.':
                        value = 0x0B;
                        break;
                    case ':':
                        value = 0x0C;
                        break;
                    default:
                        if ((value = Character.getNumericValue(pivot)) < 0 || value > 9) throw new IllegalStateException("Wrong digit " + pivot);
                        break;
                }
            } else {
                if ((value = Character.getNumericValue(pivot)) < 0 || value > 9) throw new IllegalStateException("Wrong digit " + pivot);
            }

            if (index % 2 == 0) {
                bcdByte = (byte)(value << 4); // FIXME: *10?
            } else {
                data[index/2] = (byte)(bcdByte | value);
            }

            index++;
        }

        if (index % 2 == 1) {
            data[index/2] = (byte)(bcdByte | 0x0F);
            index++;
        }

        while (index < ElkrommFacade.PHONE_NUMBER_LENGTH) {
            data[index/2] = (byte)0xff;
            index += 2;
        }

        data[14] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        data[15] = obj.getType().getValue();
        data[16] = obj.getSendingMode().getValue();
        // no events handling here!
        
        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PhoneNumber deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        int             index = 0;
        int             temp;
        char            value;
        StringBuffer    sb = new StringBuffer();

        while (index < ElkrommFacade.PHONE_NUMBER_LENGTH) {
            temp = data[index / 2];

            if (temp < 0) temp += 256;

            value = Character.toChars(temp)[0];

            if (index % 2 == 0) {
                value >>= 4;
            } else {
                value &= 0x0F;
            }

            switch (value) {
                case 0x0B:
                    value = '.';
                    break;
                case 0x0C:
                    value = ':';
                    break;
                default:
                    value += 0x30;
                    break;
            }

            if (value != 0x3F) sb.append(Character.toChars(value)[0]);

            index++;
        }

        return new org.paternostro.elkromm.dto.PhoneNumber(sb.toString(), ElkrommUtils.unpackPartitions(data[14]), Type.valueOf(data[15]), SendingMode.valueOf(data[16]), new Event[0]);
    }
}
