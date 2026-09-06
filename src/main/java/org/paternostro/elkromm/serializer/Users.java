package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Users extends Credentials
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> allocateSerializer()
    {
        return ElkrommFactory.getFactory().getUserSerializer();
    }
}
