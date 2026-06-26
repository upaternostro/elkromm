package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallFrequency;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallInterval;
import org.paternostro.elkromm.dto.PhoneParameters.Enabling;
import org.paternostro.elkromm.dto.PhoneParameters.ReturnCall;
import org.paternostro.elkromm.dto.PhoneParameters.VoiceMessagesSendingMode;

public class PhoneParametersTest {
    @Test
    public void test()
    {
        PhoneParameters pp = new PhoneParameters(Enabling.PPE_DISABLED, ReturnCall.PPRC_DISABLED, Enabling.PPE_DISABLED, VoiceMessagesSendingMode.PPVMSM_NONE, CyclicTestCallFrequency.PPCTCF_DISABLE, (byte)1, (byte)0, (byte)0, CyclicTestCallInterval.PPCTCI_1_HOUR);
        byte[]          data = ElkrommFactory.getFactory().getPhoneParametersSerializer().serialize(pp);

        assert data.length == 20 : "Wrong length";

        PhoneParameters pp2 = ElkrommFactory.getFactory().getPhoneParametersSerializer().deserialize(data);

        assert pp.getCallDelay() == pp2.getCallDelay();
        assert pp.getReturnCall() == pp2.getReturnCall();
        assert pp.getRemoteSurveillance() == pp2.getRemoteSurveillance();
        assert pp.getVoiceMessagesSendingMode() == pp2.getVoiceMessagesSendingMode();
        assert pp.getCyclicTestCallFrequency() == pp2.getCyclicTestCallFrequency();
        assert pp.getCyclicTestCallPhoneNumber() == pp2.getCyclicTestCallPhoneNumber();
        assert pp.getCyclicTestCallHour() == pp2.getCyclicTestCallHour();
        assert pp.getCyclicTestCallMinute() == pp2.getCyclicTestCallMinute();
        assert pp.getCyclicTestCallInterval() == pp2.getCyclicTestCallInterval();
    }
}
