package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.ParametersEnablings.AlarmCount;
import org.paternostro.elkromm.dto.ParametersEnablings.DST;
import org.paternostro.elkromm.dto.ParametersEnablings.Enabling;
import org.paternostro.elkromm.dto.ParametersEnablings.Help;
import org.paternostro.elkromm.dto.ParametersEnablings.Month;
import org.paternostro.elkromm.dto.ParametersEnablings.Notice;
import org.paternostro.elkromm.dto.ParametersEnablings.Play;
import org.paternostro.elkromm.dto.ParametersEnablings.PowerLack;
import org.paternostro.elkromm.dto.ParametersEnablings.Time;

public class ParametersEnablingsTest {
    @Test
    public void test()
    {
        ParametersEnablings pe = new ParametersEnablings(Time.PET_30_SECS, Time.PET_60_SECS, Time.PET_90_SECS, AlarmCount.PEAC_FOUR, PowerLack.PEPL_4_HOUR, Play.PEP_SECTS.getValue(), (byte)(Help.PEH_ENABLE.getValue() | 0x01), Enabling.PEE_ENABLE, Enabling.PEE_DISABLE, Notice.PEN_15_MINS, DST.PED_ENABLE.getValue(), Month.PEM_OCTOBER, Month.PEM_MARCH);
        byte[]              data = ElkrommFactory.getFactory().getParametersEnablingsSerializer().serialize(pe);

        assert data.length == 30 : "Wrong length";

        ParametersEnablings pe2 = ElkrommFactory.getFactory().getParametersEnablingsSerializer().deserialize(data);

        assert pe.getBulgarTime() == pe2.getBulgarTime();
        assert pe.getEmergencyTime() == pe2.getEmergencyTime();
        assert pe.getPreAlarmTime() == pe2.getPreAlarmTime();
        assert pe.getAlarmCount() == pe2.getAlarmCount();
        assert pe.getPowerLack() == pe2.getPowerLack();
        assert pe.getPlay() == pe2.getPlay();
        assert pe.getHelp() == pe2.getHelp();
        assert pe.getLan() == pe2.getLan();
        assert pe.getTimeProgrammer() == pe2.getTimeProgrammer();
        assert pe.getNotice() == pe2.getNotice();
        assert pe.getDST() == pe2.getDST();
        assert pe.getOn() == pe2.getOn();
        assert pe.getOff() == pe2.getOff();
    }
}
