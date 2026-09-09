package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommTestUtils;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Reader;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ReadersTest {
    @Test
    public void test()
    {
        Reader[]    readers = new Reader[1];
        boolean[]   associatedPartitions = { false, false, false, false, false, true, false, false };

        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Reader(i+1, 
                                        new Input(i*2 + 1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "input " + (i*2+1), Input.Delay.ID_30_SECS),
                                        new Input(i*2 + 2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, associatedPartitions, "input " + (i*2+2), Input.Delay.ID_20_SECS),
                                    ElkrommFacade.Partition.P_ONE, ElkrommFacade.Partition.P_TWO, ElkrommFacade.Partition.P_THREE, ElkrommFacade.Partition.P_FOUR, Reader.Enablings.RE_MASKING.getValue(), "Reader " + (i + 1));
        }

        byte[]  data = ElkrommFactory.getFactory().getReadersSerializer().serialize(readers);

        assert data.length == readers.length * Readers.READER_SIZE + 4 : "Wrong length";

        Reader[]    readers2 = ElkrommFactory.getFactory().getReadersSerializer().deserialize(data);

        assert readers.length == readers2.length;

        for (int i = 0; i < readers.length; i++) {
            assert readers[i].getAddress() == readers2[i].getAddress() : "Expected address " + readers[i].getAddress() + " got " + readers2[i].getAddress();

            ElkrommTestUtils.testInput(readers[i].getFirstInput(), readers2[i].getFirstInput());
            ElkrommTestUtils.testInput(readers[i].getSecondInput(), readers2[i].getSecondInput());
            
            assert readers[i].getLed1() == readers2[i].getLed1();
            assert readers[i].getLed2() == readers2[i].getLed2();
            assert readers[i].getLed3() == readers2[i].getLed3();
            assert readers[i].getLed4() == readers2[i].getLed4();
            assert readers[i].getEnablings() == readers2[i].getEnablings();
            assert readers[i].getName().equals(readers2[i].getName()) : "Expected name " + readers[i].getName() + " got " + readers2[i].getName();
        }
    }
}
