package org.paternostro.elkromm.serializer;

public class ExcludeIncludeInput extends EnableObject {
    @Override
    protected org.paternostro.elkromm.dto.EnableObject allocateEnabling(byte ordinal, boolean enabled) {
        return new org.paternostro.elkromm.dto.ExcludeIncludeInput(ordinal, enabled);
    }
}
