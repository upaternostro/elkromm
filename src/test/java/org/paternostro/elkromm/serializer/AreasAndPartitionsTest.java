package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Partition.Type;

public class AreasAndPartitionsTest {
    @Test
    public void test()
    {
        org.paternostro.elkromm.dto.AreasAndPartitions  ap = new org.paternostro.elkromm.dto.AreasAndPartitions();
        boolean[]                                       associatedPartitions = { false, false, false, false, false, false, false, false };

        for (int i = 0; i < 4; i++) {
            associatedPartitions[i] = true;
            ap.addArea(new org.paternostro.elkromm.dto.Area(i+1, "Area " + i, associatedPartitions));
            associatedPartitions[i] = false;
        }

        for (int i = 0; i < 8; i++) {
            ap.addPartition(new org.paternostro.elkromm.dto.Partition(i + 1, "Partition " + i, false, Type.STANDARD, i*10, i*20));
        }

        byte[]  data = ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().serialize(ap);

        assert data.length == 333 : "Wrong length";

        org.paternostro.elkromm.dto.AreasAndPartitions  ap2 = ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().deserialize(data);

        assert ap.getAreaNum() == ap2.getAreaNum();
        assert ap.getPartitionNum() == ap2.getPartitionNum();

        for (int i = 0; i < ap.getAreaNum(); i++) {
            assert ap.getArea(i).getName().equals(ap2.getArea(i).getName());

            for (int j = 0; j < ap.getArea(i).getAssociatedPartitions().length; j++) {
                assert ap.getArea(i).getAssociatedPartitions()[j] == ap2.getArea(i).getAssociatedPartitions()[j];
            }
            assert ap.getArea(i).getAssociatedPartitions()[i];
        }

        for (int i = 0; i < ap.getPartitionNum(); i++) {
            assert ap.getPartition(i).getName().equals(ap2.getPartition(i).getName());
            assert ap.getPartition(i).getType() == ap2.getPartition(i).getType();
            assert ap.getPartition(i).getEntryDelay() == ap2.getPartition(i).getEntryDelay();
            assert ap.getPartition(i).getExitDelay() == ap2.getPartition(i).getExitDelay();
        }
    }
}
