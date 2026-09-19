package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

/**
 * {@link org.paternostro.elkromm.dto.Key} serializer, DTO &harr; byte array.
 * <p>
 * See {@link SingleCredential} for payload structure.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleKey extends SingleCredential
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        return ElkrommFactory.getFactory().getKeySerializer();
    }
}
