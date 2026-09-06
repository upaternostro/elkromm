package org.paternostro.elkromm.serializer;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class EnableDisableUser extends EnableObject {
    @Override
    protected org.paternostro.elkromm.dto.EnableObject allocateEnabling(byte ordinal, boolean enabled) {
        return new org.paternostro.elkromm.dto.EnableDisableUser(ordinal, enabled);
    }
}
