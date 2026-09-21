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
 *  <tr><td>0x00-0x70</td><td>First reader</td><td>See {@link Reader}</td></tr>
 *  <tr><td>0x71-0xe1</td><td>Second reader</td></tr>
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

        byte[]                      data = new byte[obj.length * org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE + ElkrommUtils.CHECKSUM_SIZE];
        ElkrommSerializer<Reader>   rSerializer = ElkrommFactory.getFactory().getReaderSerializer();

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(rSerializer.serialize(obj[i]), 0, data, i * org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE, org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE);
        }

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public Reader[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE != ElkrommUtils.CHECKSUM_SIZE) throw new IllegalArgumentException("Wrong data size");

        int patchOffset = 0;
        while ((patchOffset += org.paternostro.elkromm.serializer.Reader.PSEUDO_CHECKSUM_OFFSET) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - ElkrommUtils.CHECKSUM_SIZE) {
            for (int j = 0; j < org.paternostro.elkromm.serializer.Reader.ONBOARD_INPUTS; j++) {
                data[patchOffset + org.paternostro.elkromm.serializer.Reader.FIRST_INPUT_OFFSET + j * Input.PAYLOAD_SIZE + Input.SENSITIVITY_FLAGS_OFFSET] &= ~0x10;
            }
            patchOffset += org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE;
        }

        // if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) logger.warn("Wrong checksum, expected: 0x%08x found: 0x%08x delta: 0x%08x", ElkrommUtils.computeBlockChecksum(data), ElkrommUtils.getBlockChecksum(data), ElkrommUtils.getBlockChecksum(data) - ElkrommUtils.computeBlockChecksum(data));

        Reader[]                    retval = new Reader[(data.length - ElkrommUtils.CHECKSUM_SIZE) / org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE];
        ElkrommSerializer<Reader>   rSerializer = ElkrommFactory.getFactory().getReaderSerializer();
        byte[]                      rData = new byte[org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE];

        for (int i = 0; i < data.length / org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE; i++) {
            System.arraycopy(data, i * org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE, rData, 0, org.paternostro.elkromm.serializer.Reader.PAYLOAD_SIZE);
            retval[i] = rSerializer.deserialize(rData);
        }

        return retval;
    }
}
