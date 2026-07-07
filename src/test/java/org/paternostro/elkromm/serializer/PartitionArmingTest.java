package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.PartitionArming;

public class PartitionArmingTest {
    @Test
    public void test()
    {
        PartitionArming pa = new PartitionArming((byte)(ElkrommFacade.Partition.P_ONE.getValue() | ElkrommFacade.Partition.P_THREE.getValue()), ElkrommFacade.Partition.P_THREE.getValue());
        byte[]          data = ElkrommFactory.getFactory().getPartitionArmingSerializer().serialize(pa);

        assert data.length == 2 : "Wrong length";

        PartitionArming pa2 = ElkrommFactory.getFactory().getPartitionArmingSerializer().deserialize(data);

        assert pa.getPartitions() == pa2.getPartitions();
        assert pa.getArmStatus() == pa2.getArmStatus();
    }
}
