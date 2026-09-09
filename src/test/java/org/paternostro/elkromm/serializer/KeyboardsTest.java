package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommTestUtils;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Keyboard;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class KeyboardsTest {
    @Test
    public void test()
    {
        Keyboard[]  keyboards = new Keyboard[1];
        boolean[]   associatedPartitions = { false, false, false, false, false, true, false, false };

        for (int i = 0; i < keyboards.length; i++) {
            keyboards[i] = new Keyboard(i+1, "2.71", 
                                            new Input(i*2 + 1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input " + (i*2+1), Input.Delay.ID_30_SECS),
                                            new Input(i*2 + 2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input " + (i*2+2), Input.Delay.ID_20_SECS),
                                        Keyboard.Enablings.KE_ENTRY.getValue(), associatedPartitions, Keyboard.AudioFeatures.KA_NONE.getValue(), "Keyboard " + (i + 1));
        }

        byte[]  data = ElkrommFactory.getFactory().getKeyboardsSerializer().serialize(keyboards);

        assert data.length == keyboards.length * Keyboards.KEYBOARD_SIZE + 4 : "Wrong length";

        Keyboard[]  keyboards2 = ElkrommFactory.getFactory().getKeyboardsSerializer().deserialize(data);

        assert keyboards.length == keyboards2.length;

        for (int i = 0; i < keyboards.length; i++) {
            assert keyboards[i].getAddress() == keyboards2[i].getAddress() : "Expected address " + keyboards[i].getAddress() + " got " + keyboards2[i].getAddress();
            assert keyboards[i].getVersion().equals(keyboards2[i].getVersion()) : "Expected version " + keyboards[i].getVersion() + " got " + keyboards2[i].getVersion();

            ElkrommTestUtils.testInput(keyboards[i].getFirstInput(), keyboards2[i].getFirstInput());
            ElkrommTestUtils.testInput(keyboards[i].getSecondInput(), keyboards2[i].getSecondInput());
            
            assert keyboards[i].getEnablings() == keyboards2[i].getEnablings();

            for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
                assert keyboards[i].getAssociatedPartitions()[k] == keyboards2[i].getAssociatedPartitions()[k];
            }

            assert keyboards[i].getAudioFeatures() == keyboards2[i].getAudioFeatures();
            assert keyboards[i].getName().equals(keyboards2[i].getName()) : "Expected name " + keyboards[i].getName() + " got " + keyboards2[i].getName();
        }
    }
}
