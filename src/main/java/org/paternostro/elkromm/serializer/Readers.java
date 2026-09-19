package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Reader}s serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: at most {@link ElkrommFacade#MAX_READERS} instances of {@link Reader},
 * each containing:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-112</td><td>First reader</td><td>See {@link Reader}</td></tr>
 *  <tr><td>113-225</td><td>Second reader</td></tr>
 *  <tr><td>...</td></tr>
 *  <tr><td>x-3,x</td><td>Checksum</td><td>Last four bytes are block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Reader
 */
public class Readers implements ElkrommSerializer<Reader[]>
{
    public static final Logger logger = LoggerFactory.getLogger(Readers.class);

    @Override
    public byte[] serialize(Reader[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * SerializersConstants.READER_SIZE + 4];
        ElkrommSerializer<Reader>   rSerializer = ElkrommFactory.getFactory().getReaderSerializer();

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(rSerializer.serialize(obj[i]), 0, data, i * SerializersConstants.READER_SIZE, SerializersConstants.READER_SIZE);
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public Reader[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % SerializersConstants.READER_SIZE != 4) throw new IllegalArgumentException("Wrong data size");

        int patchOffset = 0;
        while ((patchOffset += SerializersConstants.READER_SIZE - 2) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - 4) {
            for (int j = 0; j < 2; j++) {
                data[patchOffset + 6 + j * SerializersConstants.INPUT_SIZE + 3] &= ~0x10;
            }
            patchOffset += SerializersConstants.READER_SIZE;
        }

        // if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) logger.warn("Wrong checksum, expected: 0x%08x found: 0x%08x delta: 0x%08x", ElkrommUtils.computeBlockChecksum(data), ElkrommUtils.getLong(data, data.length - 4), ElkrommUtils.getLong(data, data.length - 4) - ElkrommUtils.computeBlockChecksum(data));

        Reader[]                    retval = new Reader[(data.length - 4) / SerializersConstants.READER_SIZE];
        ElkrommSerializer<Reader>   rSerializer = ElkrommFactory.getFactory().getReaderSerializer();
        byte[]                      rData = new byte[SerializersConstants.READER_SIZE];

        for (int i = 0; i < data.length / SerializersConstants.READER_SIZE; i++) {
            System.arraycopy(data, i * SerializersConstants.READER_SIZE, rData, 0, SerializersConstants.READER_SIZE);
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
