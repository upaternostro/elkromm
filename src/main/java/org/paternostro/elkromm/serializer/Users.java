package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

/**
 * {@link org.paternostro.elkromm.dto.User}s serializer, DTO &harr; byte array.
 * <p>
 * Payload structure: see {@link User}
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see User
 */
public class Users extends Credentials
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> allocateSerializer()
    {
        return ElkrommFactory.getFactory().getUserSerializer();
    }
}
