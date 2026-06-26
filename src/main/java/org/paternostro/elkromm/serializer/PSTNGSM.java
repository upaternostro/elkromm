package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PSTNGSM.Country;
import org.paternostro.elkromm.dto.PSTNGSM.Enabling;
import org.paternostro.elkromm.dto.PSTNGSM.PABXLocalAccessDigit;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNAnsweringMachineRings;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNLineTestFrequency;

public class PSTNGSM implements ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PSTNGSM obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[length()];

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
        
        int offset = 12;
        for (byte pivot : ElkrommUtils.bcd(obj.getGSMPin(), 6)) {
            data[offset++] = pivot;
        }

        data[15] = obj.getExpirationMonth();
        data[16] = obj.getExpirationYear();

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PSTNGSM deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        return new org.paternostro.elkromm.dto.PSTNGSM(Enabling.valueOf(data[0]), Country.valueOf(data[1]), PABXLocalAccessDigit.valueOf(data[4]), Enabling.valueOf(data[5]), Enabling.valueOf(data[6]), PSTNLineTestFrequency.valueOf(data[7]), PSTNAnsweringMachineRings.valueOf(data[8]), Enabling.valueOf(data[9]), Enabling.valueOf(data[10]), Enabling.valueOf(data[11]), ElkrommUtils.dcb(data, 12, 3), data[15], data[16]);
    }

    @Override
    public int length()
    {
        return 21;
    }
}
