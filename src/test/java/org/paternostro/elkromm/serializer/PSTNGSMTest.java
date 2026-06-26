package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.PSTNGSM.Country;
import org.paternostro.elkromm.dto.PSTNGSM.Enabling;
import org.paternostro.elkromm.dto.PSTNGSM.PABXLocalAccessDigit;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNAnsweringMachineRings;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNLineTestFrequency;

public class PSTNGSMTest {
    @Test
    public void test()
    {
        PSTNGSM pg = new PSTNGSM(Enabling.PGE_DISABLED, Country.PGC_ITALY, PABXLocalAccessDigit.PGPLAD_DISABLE, Enabling.PGE_DISABLED, Enabling.PGE_DISABLED, PSTNLineTestFrequency.PGPLTF_DISABLE, PSTNAnsweringMachineRings.PGPAMR_DISABLE, Enabling.PGE_DISABLED, Enabling.PGE_DISABLED, Enabling.PGE_DISABLED, 0, (byte)1, (byte)0);
        byte[]  data = ElkrommFactory.getFactory().getPSTNGSMSerializer().serialize(pg);

        assert data.length == 21 : "Wrong length";

        PSTNGSM pg2 = ElkrommFactory.getFactory().getPSTNGSMSerializer().deserialize(data);

        assert pg.getEnablePSTN() == pg2.getEnablePSTN();
        assert pg.getCountry() == pg2.getCountry();
        assert pg.getPABXLocalAccessDigit() == pg2.getPABXLocalAccessDigit();
        assert pg.getToneControl() == pg2.getToneControl();
        assert pg.getAnswerControl() == pg2.getAnswerControl();
        assert pg.getPSTNLineTestFrequency() == pg2.getPSTNLineTestFrequency();
        assert pg.getPSTNAnsweringMachineRings() == pg2.getPSTNAnsweringMachineRings();
        assert pg.getEnableGSM() == pg2.getEnableGSM();
        assert pg.getEnableGSMAnsweringMachine() == pg2.getEnableGSMAnsweringMachine();
        assert pg.getEnableIncomingSMS() == pg2.getEnableGSMAnsweringMachine();
        assert pg.getGSMPin() == pg2.getGSMPin();
        assert pg.getExpirationMonth() == pg2.getExpirationMonth();
        assert pg.getExpirationYear() == pg2.getExpirationYear();
    }
}
