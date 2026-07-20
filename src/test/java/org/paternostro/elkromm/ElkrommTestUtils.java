package org.paternostro.elkromm;

import org.paternostro.elkromm.dto.Input;

public class ElkrommTestUtils {
    public static void testInput(Input input, Input input2)
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
