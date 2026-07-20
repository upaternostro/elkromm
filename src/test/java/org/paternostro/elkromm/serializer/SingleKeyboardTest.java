package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommTestUtils;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.SingleKeyboard;

public class SingleKeyboardTest {
    @Test
    public void test()
    {
        boolean[]       associatedPartitions = { false, false, false, false, false, true, false, false };
        SingleKeyboard  singleKeyboard = new SingleKeyboard((byte)1, new Keyboard(1, "2.71", 
                                        new Input(1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input 1", Input.Delay.ID_30_SECS),
                                        new Input(2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input 2", Input.Delay.ID_20_SECS),
                                    Keyboard.Enablings.KE_ENTRY.getValue(), associatedPartitions, Keyboard.AudioFeatures.KA_NONE.getValue(), "Keyboard 1" ));

        byte[]  data = ElkrommFactory.getFactory().getSingleKeyboardSerializer().serialize(singleKeyboard);

        assert data.length == 112 : "Wrong length";

        SingleKeyboard  singleKeyboard2 = ElkrommFactory.getFactory().getSingleKeyboardSerializer().deserialize(data);

        assert singleKeyboard.getIndex() == singleKeyboard2.getIndex() : "Expected " + singleKeyboard.getIndex() + " got " + singleKeyboard2.getIndex();
        assert singleKeyboard.getKeyboard().getAddress() == singleKeyboard2.getKeyboard().getAddress() : "Expected address " + singleKeyboard.getKeyboard().getAddress() + " got " + singleKeyboard2.getKeyboard().getAddress();
        assert singleKeyboard.getKeyboard().getVersion().equals(singleKeyboard2.getKeyboard().getVersion()) : "Expected version " + singleKeyboard.getKeyboard().getVersion() + " got " + singleKeyboard2.getKeyboard().getVersion();

        ElkrommTestUtils.testInput(singleKeyboard.getKeyboard().getFirstInput(), singleKeyboard2.getKeyboard().getFirstInput());
        ElkrommTestUtils.testInput(singleKeyboard.getKeyboard().getSecondInput(), singleKeyboard2.getKeyboard().getSecondInput());
        
        assert singleKeyboard.getKeyboard().getEnablings() == singleKeyboard2.getKeyboard().getEnablings();

        for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
            assert singleKeyboard.getKeyboard().getAssociatedPartitions()[k] == singleKeyboard2.getKeyboard().getAssociatedPartitions()[k];
        }

        assert singleKeyboard.getKeyboard().getAudioFeatures() == singleKeyboard2.getKeyboard().getAudioFeatures();
        assert singleKeyboard.getKeyboard().getName().equals(singleKeyboard2.getKeyboard().getName()) : "Expected name " + singleKeyboard.getKeyboard().getName() + " got " + singleKeyboard2.getKeyboard().getName();
    }
}
