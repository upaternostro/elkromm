package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PhoneNumber.Event;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneNumbersSendingCodes implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes>
{
    public static final int EXPANSION_SIZE  = 559;
    public static final int INPUT_SIZE      = 38;
    public static final int PHONE_NUMBER_SIZE     = 17;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneNumbersSendingCodes obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                      data = new byte[length()];
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>  pnSerializer = ElkrommFactory.getFactory().getPhoneNumberSerializer();

        for (int i = 0; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            System.arraycopy(pnSerializer.serialize(obj.getPhoneNumbers()[i]), 0, data, i * PHONE_NUMBER_SIZE, PHONE_NUMBER_SIZE);
        }

        int value;

        for (Event pivot : Event.values()) {
            ElkrommUtils.setWord(data, pivot.getOffset(), value = obj.getAssignedEventMask(pivot));

            switch (pivot) {
                case PNSCE_BURGLAR_ALARM: // prima occorrenza, poi e8, ec, f0
                    ElkrommUtils.setWord(data, 0x00e8, value);
                    ElkrommUtils.setWord(data, 0x00ec, value);
                    ElkrommUtils.setWord(data, 0x00f0, value);
                    break;
                case PNSCE_PARTITIONS_SYSTEM_ON_OFF: // prima occorrenza, poi 138, 140
                    ElkrommUtils.setWord(data, 0x0138, value);
                    ElkrommUtils.setWord(data, 0x0140, value);
                    break;
                case PNSCE_INPUT_INCLUSION_EXCLUSION: // prima occorrenza, poi 148
                    ElkrommUtils.setWord(data, 0x0148, value);
                    break;
                case PNSCE_TAMPERING: // prima occorrenza, poi 104, 170
                    ElkrommUtils.setWord(data, 0x0104, value);
                    ElkrommUtils.setWord(data, 0x0170, value);
                    break;
                case PNSCE_SYSTEM_FAULT: // prima occorrenza, poi 130
                    ElkrommUtils.setWord(data, 0x0130, value);
                    break;
                default:
                    break;
            }
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PhoneNumbersSendingCodes deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        org.paternostro.elkromm.dto.PhoneNumber[]                   phoneNumbers = new org.paternostro.elkromm.dto.PhoneNumber[ElkrommFacade.MAX_PHONE_NUMBERS];
        int                                                         offset;
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>  pnSerializer = ElkrommFactory.getFactory().getPhoneNumberSerializer();
        byte[]                                                      pnData = new byte[PHONE_NUMBER_SIZE];

        for (int i = 0; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            offset = i * PHONE_NUMBER_SIZE;

            System.arraycopy(data, offset, pnData, 0, PHONE_NUMBER_SIZE);
            phoneNumbers[i] = pnSerializer.deserialize(pnData);
        }
        
        int     value;
        int     mask;
        Event[] events = new Event[1];

        for (Event pivot : Event.values()) {
            events[0] = pivot;
            value = ElkrommUtils.getWord(data, pivot.getOffset());
            mask = 0x0001;

            for (int j = 0; j < ElkrommFacade.MAX_PHONE_NUMBERS; j++) {
                if ((value & mask) != 0) phoneNumbers[j].addAssignedEvents(events);
                mask <<= 1;
            }
        }

        return new org.paternostro.elkromm.dto.PhoneNumbersSendingCodes(phoneNumbers);
    }

    @Override
    public int length()
    {
        return 408;
    }
}
