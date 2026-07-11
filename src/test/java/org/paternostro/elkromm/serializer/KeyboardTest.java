package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Keyboard;

public class KeyboardTest {
    @Test
    public void test()
    {
        boolean[]   associatedPartitions = { false, false, false, false, false, true, false, false };

        Keyboard keyboard = new Keyboard(1, "2.71", 
                                        new Input(1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input 1", Input.Delay.ID_30_SECS),
                                        new Input(2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input 2", Input.Delay.ID_20_SECS),
                                    Keyboard.Enablings.KE_ENTRY.getValue(), associatedPartitions, Keyboard.AudioFeatures.KA_NONE.getValue(), "Keyboard 1" );

        byte[]  data = ElkrommFactory.getFactory().getKeyboardSerializer().serialize(keyboard);

        assert data.length == 111 : "Wrong length";

        Keyboard  keyboard2 = ElkrommFactory.getFactory().getKeyboardSerializer().deserialize(data);

        assert keyboard.getAddress() == keyboard2.getAddress() : "Expected address " + keyboard.getAddress() + " got " + keyboard2.getAddress();
        assert keyboard.getVersion().equals(keyboard2.getVersion()) : "Expected version " + keyboard.getVersion() + " got " + keyboard2.getVersion();

        testInput(keyboard.getFirstInput(), keyboard2.getFirstInput());
        testInput(keyboard.getSecondInput(), keyboard2.getSecondInput());
        
        assert keyboard.getEnablings() == keyboard2.getEnablings();

        for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
            assert keyboard.getAssociatedPartitions()[k] == keyboard2.getAssociatedPartitions()[k];
        }

        assert keyboard.getAudioFeatures() == keyboard2.getAudioFeatures();
        assert keyboard.getName().equals(keyboard2.getName()) : "Expected name " + keyboard.getName() + " got " + keyboard2.getName();
    }

    private void testInput(Input input, Input input2)
    {
        assert input.getLogicNumber() == input2.getLogicNumber();
        assert input.getConfiguration() == input2.getConfiguration();
        assert input.getSpecialization() == input2.getSpecialization();

        for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
            assert input.getAssociatedPartitions()[k] == input2.getAssociatedPartitions()[k];
        }

        assert input.getName().equals(input2.getName());
    }
}
