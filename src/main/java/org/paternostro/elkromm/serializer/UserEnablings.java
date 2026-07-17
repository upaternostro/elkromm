package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;

public class UserEnablings implements ElkrommSerializer<org.paternostro.elkromm.dto.UserEnablings>
{
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

        byte[]  data = new byte[length()];

        ElkrommUtils.setLong(data, 0, enablings);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.UserEnablings deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        int         enablings = ElkrommUtils.getLong(data, 0);
        boolean[]   userEnablings = new boolean[ElkrommFacade.MAX_CREDENTIALS];

        for (int i = 31; i >= 0; i--) {
            userEnablings[31 - i] = (enablings & (1 << i)) != 0;
        }

        return new org.paternostro.elkromm.dto.UserEnablings(userEnablings);
    }

    @Override
    public int length() {
        return 4;
    }
}
