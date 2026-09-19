package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

/**
 * {@link org.paternostro.elkromm.dto.Key}s serializer, DTO &harr; byte array.
 * <p>
 * Payload structure: see {@link Key}
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Key
 */
public class Keys extends Credentials
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> allocateSerializer()
    {
        return ElkrommFactory.getFactory().getKeySerializer();
    }
}
