package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Reader;

public class Readers implements ElkrommSerializer<Reader[]>
{
    public static final int READER_SIZE = 113;
    public static final int INPUT_SIZE  = 38;

    @Override
    public byte[] serialize(Reader[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * READER_SIZE + 4];
        ElkrommSerializer<Reader>   rSerializer = ElkrommFactory.getFactory().getReaderSerializer();

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(rSerializer.serialize(obj[i]), 0, data, i * READER_SIZE, READER_SIZE);
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public Reader[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % READER_SIZE != 4) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Reader[]                    retval = new Reader[(data.length - 4) / READER_SIZE];
        ElkrommSerializer<Reader>   rSerializer = ElkrommFactory.getFactory().getReaderSerializer();
        byte[]                      rData = new byte[READER_SIZE];

        for (int i = 0; i < data.length / READER_SIZE; i++) {
            System.arraycopy(data, i * READER_SIZE, rData, 0, READER_SIZE);
            retval[i] = rSerializer.deserialize(rData);
        }

        return retval;
    }

    @Override
    public int length()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'length'");
    }
}
