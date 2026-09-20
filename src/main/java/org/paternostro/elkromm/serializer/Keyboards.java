package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Keyboard;

/**
 * {@link Keyboard}s serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: one or more {@link Keyboard}, max {@link org.paternostro.elkromm.ElkrommFacade#MAX_KEYPADS} keyboards.
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-110</td><td>First keyboard</td><td>See {@link Keyboard}</td></tr>
 *  <tr><td>111-221</td><td>Second keyboard</td><td>The preceding fields repeat</td></tr>
 *  <tr><td>...</td></tr>
 *  <tr><td>x-3,x</td><td>Checksum</td><td>Last four bytes are block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Keyboard
 */
public class Keyboards implements ElkrommSerializer<Keyboard[]>
{
    @Override
    public byte[] serialize(Keyboard[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE + ElkrommUtils.CHECKSUM_SIZE];
        ElkrommSerializer<Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();

        Arrays.fill(data, (byte)0x00);

        for (int i = 0; i < obj.length; i++) {
            System.arraycopy(kSerializer.serialize(obj[i]), 0, data, i * org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE, org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE);
        }

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public Keyboard[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE != ElkrommUtils.CHECKSUM_SIZE) throw new IllegalArgumentException("Wrong data size");

        int patchOffset = 0;
        while ((patchOffset += org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE - 2) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - ElkrommUtils.CHECKSUM_SIZE) {
            for (int j = 0; j < 2; j++) {
                data[patchOffset + 6 + j * Input.PAYLOAD_SIZE + 3] &= ~0x10;
            }
            patchOffset += org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE;
        }

        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        Keyboard[]                  retval = new Keyboard[(data.length - ElkrommUtils.CHECKSUM_SIZE) / org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE];
        ElkrommSerializer<Keyboard> kSerializer = ElkrommFactory.getFactory().getKeyboardSerializer();
        byte[]                      kData = new byte[org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE];

        for (int i = 0; i < data.length / org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE; i++) {
            System.arraycopy(data, i * org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE, kData, 0, org.paternostro.elkromm.serializer.Keyboard.PAYLOAD_SIZE);
            retval[i] = kSerializer.deserialize(kData);
        }

        return retval;
    }
}
