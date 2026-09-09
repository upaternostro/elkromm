package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Checksums;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ChecksumsTest {
    @Test
    public void test()
    {
        Checksums  checksums = new Checksums(
            0x01020304,
            0x02030405,
            0x03040506,
            0x04050607,
            0x05060708,
            0x06070809,
            0x0708090a,
            0x08090a0b,
            0x090a0b0c,
            0x0a0b0c0d,
            0x0b0c0d0e,
            0x0c0d0e0f,
            0x0d0e0f10
        );

        byte[]  data = ElkrommFactory.getFactory().getChecksumsSerializer().serialize(checksums);

        assert data.length == 52 : "Wrong length";

        Checksums  checksums2 = ElkrommFactory.getFactory().getChecksumsSerializer().deserialize(data);

        assert checksums.getNodes() == checksums2.getNodes();
        assert checksums.getKeypads() == checksums2.getKeypads();
        assert checksums.getReaders() == checksums2.getReaders();
        assert checksums.getSystem() == checksums2.getSystem();
        assert checksums.getTimeProgrammer() == checksums2.getTimeProgrammer();
        assert checksums.getAreasAndPartitions() == checksums2.getAreasAndPartitions();
        assert checksums.getTelephoneParameters() == checksums2.getTelephoneParameters();
        assert checksums.getTelephoneNumbers() == checksums2.getTelephoneNumbers();
        assert checksums.getEvents() == checksums2.getEvents();
        assert checksums.getSms() == checksums2.getSms();
        assert checksums.getPstnGsm() == checksums2.getPstnGsm();
        assert checksums.getUsers() == checksums2.getUsers();
        assert checksums.getKeys() == checksums2.getKeys();
    }
}
