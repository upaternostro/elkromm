package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.ExcludeIncludeInput} serializer, DTO &harr; byte array.
 * <p>
 * See {@link EnableObject} for payload structure.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ExcludeIncludeInput extends EnableObject {
    @Override
    protected org.paternostro.elkromm.dto.EnableObject allocateEnabling(byte ordinal, boolean enabled) {
        return new org.paternostro.elkromm.dto.ExcludeIncludeInput(ordinal, enabled);
    }
}
