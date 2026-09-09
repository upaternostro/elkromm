package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Output;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class OutputTest {
    @Test
    public void test()
    {
        boolean[]   associatedPartitions = { false, false, false, false, true, false, false, false };
        Output      output = new Output(69, Output.Type.OT_NORMALLY_HIGH, associatedPartitions, Output.Specialization.OS_TAMPERING, "Input 42");
        byte[]      data = ElkrommFactory.getFactory().getOutputSerializer().serialize(output);

        assert data.length == 37 : "Wrong length";

        Output      output2 = ElkrommFactory.getFactory().getOutputSerializer().deserialize(data);

        assert output.getLogicNumber() == output2.getLogicNumber();
        assert output.getType() == output2.getType();

        for (int i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            assert output.getAssociatedPartitions()[i] == output2.getAssociatedPartitions()[i];
        }

        assert output.getSpecialization() == output2.getSpecialization();
        assert output.getName().equals(output2.getName());
    }
}
