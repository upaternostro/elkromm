package org.paternostro.elkromm.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.Partition;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class AreasAndPartitionsTest {
    @Test
    public void test()
    {
        org.paternostro.elkromm.dto.AreasAndPartitions  ap = new org.paternostro.elkromm.dto.AreasAndPartitions();
        boolean[]                                       associatedPartitions = { false, false, false, false, false, false, false, false };

        for (int i = 0; i < 4; i++) {
            associatedPartitions[i] = true;
            ap.addArea(new Area("Area " + i, associatedPartitions));
            associatedPartitions[i] = false;
        }

        for (int i = 0; i < 8; i++) {
            ap.addPartition(new Partition("Partition " + i, false, Partition.Type.STANDARD, Partition.EntryExitDelay.values()[i % Partition.EntryExitDelay.values().length], Partition.EntryExitDelay.values()[(i + 1) % Partition.EntryExitDelay.values().length]));
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

    @Test
    public void fiveMinutesTest()
    {
        org.paternostro.elkromm.dto.AreasAndPartitions  ap = new org.paternostro.elkromm.dto.AreasAndPartitions();
        /*
         * Ho provato un test JUnit che serializza una partizione con PD_FIVE_MINUTES e
         * controlla che il primo slot valga 00 e6 (d[off]==0x00 e d[off+1]==0xE6). Con
         * la tua patch com'è fallisce ("expected 0 but was 255"), con & 0xFF passa.
         * Aggiungerlo ad AreasAndPartitionsTest fissa l'unico valore non ovvio.
         */
        ap.addPartition(new Partition("a partition", false, Partition.Type.STANDARD, Partition.EntryExitDelay.PD_FIVE_MINUTES, Partition.EntryExitDelay.PD_FIVE_MINUTES));

        byte[]  data = ElkrommFactory.getFactory().getAreasAndPartitionsSerializer().serialize(ap);

        assertEquals(0,                                data[AreasAndPartitions.PARTITIONS_ENTRY_DELAYS_OFFSET]);
        assertEquals(Partition.EntryExitDelay.PD_FIVE_MINUTES.getValue(), data[AreasAndPartitions.PARTITIONS_ENTRY_DELAYS_OFFSET + 1]);
        assertEquals(0,                                data[AreasAndPartitions.PARTITIONS_EXIT_DELAYS_OFFSET]);
        assertEquals(Partition.EntryExitDelay.PD_FIVE_MINUTES.getValue(), data[AreasAndPartitions.PARTITIONS_EXIT_DELAYS_OFFSET + 1]);
    }
}
