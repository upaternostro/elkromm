package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PhoneNumber.Event;
import org.paternostro.elkromm.dto.PhoneNumber.SendingMode;
import org.paternostro.elkromm.dto.PhoneNumber.Type;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneNumber implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneNumber obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];
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
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

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

    @Override
    public int length()
    {
        return 17;
    }
}
