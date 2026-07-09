package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.ParametersEnablings.AlarmCount;
import org.paternostro.elkromm.dto.ParametersEnablings.Enabling;
import org.paternostro.elkromm.dto.ParametersEnablings.Month;
import org.paternostro.elkromm.dto.ParametersEnablings.Notice;
import org.paternostro.elkromm.dto.ParametersEnablings.PowerLack;
import org.paternostro.elkromm.dto.ParametersEnablings.Time;

public class ParametersEnablings implements ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.ParametersEnablings obj) {
        byte[]  data = new byte[length()];

        // byte 5, 7 and 9 contain the same value
        data[ 5] = 
        data[ 7] = 
        data[ 9] = obj.getBulgarTime().getValue();
        data[ 6] = obj.getPreAlarmTime().getValue();
        data[ 8] = obj.getEmergencyTime().getValue();
        data[11] = obj.getPowerLack().getValue();
        data[13] = obj.getAlarmCount().getValue();
        data[14] = obj.getNotice().getValue();
        data[15] = obj.getTimeProgrammer().getValue();
        data[16] = obj.getDST();
        data[21] = obj.getOff().getValue();
        data[22] = obj.getOn().getValue();
        data[23] = obj.getLan().getValue();
        data[24] = obj.getPlay();
        data[25] = obj.getHelp();
        
        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.ParametersEnablings deserialize(byte[] data) {
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        return new org.paternostro.elkromm.dto.ParametersEnablings(Time.valueOf(data[5]), Time.valueOf(data[8]), Time.valueOf(data[6]), AlarmCount.valueOf(data[13]), PowerLack.valueOf(data[11]), data[24], data[25], Enabling.valueOf(data[23]), Enabling.valueOf(data[15]), Notice.valueOf(data[14]), data[16], Month.valueOf(data[22]), Month.valueOf(data[21]));
    }

    @Override
    public int length() {
        return 30;
    }
}
