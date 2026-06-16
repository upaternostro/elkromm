package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;

public class Checksums implements ElkrommSerializer<org.paternostro.elkromm.dto.Checksums>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Checksums obj) {
        byte[]  data = new byte[length()];

        ElkrommUtils.setLong(data,  0, obj.getNodes());
        ElkrommUtils.setLong(data,  4, obj.getKeypads());
        ElkrommUtils.setLong(data,  8, obj.getReaders());
        ElkrommUtils.setLong(data, 12, obj.getSystem());
        ElkrommUtils.setLong(data, 16, obj.getTimeProgrammer());
        ElkrommUtils.setLong(data, 20, obj.getAreasAndPartitions());
        ElkrommUtils.setLong(data, 24, obj.getTelephoneParameters());
        ElkrommUtils.setLong(data, 28, obj.getTelephoneNumbers());
        ElkrommUtils.setLong(data, 32, obj.getEvents());
        ElkrommUtils.setLong(data, 36, obj.getSms());
        ElkrommUtils.setLong(data, 40, obj.getPstnGsm());
        ElkrommUtils.setLong(data, 44, obj.getUsers());
        ElkrommUtils.setLong(data, 48, obj.getKeys());

        // no checksum here

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Checksums deserialize(byte[] data) {
        // no checksum here
        return new org.paternostro.elkromm.dto.Checksums(
            ElkrommUtils.getLong(data,  0),
            ElkrommUtils.getLong(data,  4),
            ElkrommUtils.getLong(data,  8),
            ElkrommUtils.getLong(data, 12),
            ElkrommUtils.getLong(data, 16),
            ElkrommUtils.getLong(data, 20),
            ElkrommUtils.getLong(data, 24),
            ElkrommUtils.getLong(data, 28),
            ElkrommUtils.getLong(data, 32),
            ElkrommUtils.getLong(data, 36),
            ElkrommUtils.getLong(data, 40),
            ElkrommUtils.getLong(data, 44),
            ElkrommUtils.getLong(data, 48)
        );
    }

    @Override
    public int length() {
        return 13*4;
    }
}
