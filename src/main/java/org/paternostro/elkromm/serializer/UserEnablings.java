package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.UserEnablings} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x03</td><td>Enabling</td><td>A single long word containing {@link ElkrommFacade#MAX_CREDENTIALS} flags, one for each user, starting with user 1 (TECHNICAL/INSTALLER) at MSB</td><td>{@link #ENABLINGS_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class UserEnablings implements ElkrommSerializer<org.paternostro.elkromm.dto.UserEnablings>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 4;

    /** Offset of the enablings word */
    public static final int ENABLINGS_OFFSET = 0x00;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.UserEnablings obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        int         enablings = 0;
        boolean[]   userEnablings = obj.getEnablings();

        for (int i = 31; i >= 0; i--) {
            if (userEnablings[31 - i]) {
                enablings |= (1 << i);
            }
        }

        byte[]  data = new byte[PAYLOAD_SIZE];

        ElkrommUtils.setLong(data, ENABLINGS_OFFSET, enablings);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.UserEnablings deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        int         enablings = ElkrommUtils.getLong(data, ENABLINGS_OFFSET);
        boolean[]   userEnablings = new boolean[ElkrommFacade.MAX_CREDENTIALS];

        for (int i = 31; i >= 0; i--) {
            userEnablings[31 - i] = (enablings & (1 << i)) != 0;
        }

        return new org.paternostro.elkromm.dto.UserEnablings(userEnablings);
    }
}
