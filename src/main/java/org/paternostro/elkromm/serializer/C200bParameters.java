package org.paternostro.elkromm.serializer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.C200bParameters.Event;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class C200bParameters implements ElkrommSerializer<org.paternostro.elkromm.dto.C200bParameters> 
{
    public static final int INPUT_CODES_OFFSET = 0x64;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.C200bParameters obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

        for (Entry<Event,Byte> pivot : obj.getEventCodes().entrySet()) {
            data[pivot.getKey().getOffset()] = pivot.getValue();

            switch (pivot.getKey()) {
                case C2PE_BURGLAR_ALARM:
                    data[0x39] = pivot.getValue();
                    data[0x3a] = pivot.getValue();
                    data[0x3b] = pivot.getValue();
                    break;
                case C2PE_SYSTEM_ON_OFF:
                    data[0x4d] = pivot.getValue();
                    data[0x4f] = pivot.getValue();
                    break;
                case C2PE_INPUT_INCLUSION_EXCLUSION:
                    data[0x51] = pivot.getValue();
                    break;
                case C2PE_TAMPERING:
                    data[0x40] = pivot.getValue();
                    data[0x5b] = pivot.getValue();
                    break;
                case C2PE_SYSTEM_FAULT:
                    data[0x4b] = pivot.getValue();
                    break;
                default:
                    break;
            }
        }

        System.arraycopy(obj.getInputCodes(), 0, data, INPUT_CODES_OFFSET, ElkrommFacade.MAX_LOGICAL_INPUTS);

        data[0x33] = data[0x36] = data[0x37] = data[0x3d] = data[0x3e] = data[0x3f] = data[0x44] = data[0x45] = data[0x46] = data[0x49] =
        data[0x55] = data[0x56] = data[0x57] = data[0x5c] = data[0x5d] = data[0x5e] = data[0x5f] = data[0x60] = data[0x61] = data[0x62] = 
        data[0x63] = (byte)0xff;

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.C200bParameters deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data length");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        org.paternostro.elkromm.dto.C200bParameters retval = new org.paternostro.elkromm.dto.C200bParameters(new HashMap<>(), new byte[0]);

        for (Event pivot : Event.values()) {
            retval.setEventCode(pivot, data[pivot.getOffset()]);
        }

        for (int i = 0; i < ElkrommFacade.MAX_LOGICAL_INPUTS; i++) {
            retval.setInputCode(i, data[i + INPUT_CODES_OFFSET]);
        }

        return retval;
    }

    @Override
    public int length() {
        return 168;
    }
}
