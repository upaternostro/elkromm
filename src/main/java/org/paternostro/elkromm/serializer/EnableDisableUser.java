package org.paternostro.elkromm.serializer;

public class EnableDisableUser extends EnableObject {
    @Override
    protected org.paternostro.elkromm.dto.EnableObject allocateEnabling(byte ordinal, boolean enabled) {
        return new org.paternostro.elkromm.dto.EnableDisableUser(ordinal, enabled);
    }
}
