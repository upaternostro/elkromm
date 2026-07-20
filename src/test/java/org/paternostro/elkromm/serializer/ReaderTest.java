package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommTestUtils;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Reader;

public class ReaderTest {
    @Test
    public void test()
    {
        boolean[]   associatedPartitions = { false, false, false, false, false, true, false, false };

        Reader  reader = new Reader(1, 
                                    new Input(1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input 1", Input.Delay.ID_30_SECS),
                                    new Input(2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input 2", Input.Delay.ID_20_SECS),
                                ElkrommFacade.Partition.P_ONE, ElkrommFacade.Partition.P_TWO, ElkrommFacade.Partition.P_THREE, ElkrommFacade.Partition.P_FOUR, Reader.Enablings.RE_MASKING.getValue(), "Reader 1");

        byte[]  data = ElkrommFactory.getFactory().getReaderSerializer().serialize(reader);

        assert data.length == 113 : "Wrong length";

        Reader    reader2 = ElkrommFactory.getFactory().getReaderSerializer().deserialize(data);

        assert reader.getAddress() == reader2.getAddress() : "Expected address " + reader.getAddress() + " got " + reader2.getAddress();

        ElkrommTestUtils.testInput(reader.getFirstInput(), reader2.getFirstInput());
        ElkrommTestUtils.testInput(reader.getSecondInput(), reader2.getSecondInput());
        
        assert reader.getLed1() == reader2.getLed1();
        assert reader.getLed2() == reader2.getLed2();
        assert reader.getLed3() == reader2.getLed3();
        assert reader.getLed4() == reader2.getLed4();
        assert reader.getEnablings() == reader2.getEnablings();
        assert reader.getName().equals(reader2.getName()) : "Expected name " + reader.getName() + " got " + reader2.getName();
    }
}
