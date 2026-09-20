package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Input;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class InputTest {
    @Test
    public void test()
    {
        boolean[]   associatedPartitions = { false, false, true, false, false, false, false, false };
        Input       input = new Input(42, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_IMMEDIATE, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "Input 42", Input.Delay.ID_10_SECS);
        byte[]      data = ElkrommFactory.getFactory().getInputSerializer().serialize(input);

        assert data.length == 38 : "Wrong length";

        Input       input2 = ElkrommFactory.getFactory().getInputSerializer().deserialize(data);

        assert input.getLogicNumber() == input2.getLogicNumber();
        assert input.getConfiguration() == input2.getConfiguration();
        assert input.getSpecialization() == input2.getSpecialization();
        assert input.getFlags() == input2.getFlags();
        assert input.getVideo() == input2.getVideo();

        for (int i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            assert input.getAssociatedPartitions()[i] == input2.getAssociatedPartitions()[i];
        }

        assert input.getName().equals(input2.getName());
        assert input.getDelay() == input2.getDelay();
    }
}
