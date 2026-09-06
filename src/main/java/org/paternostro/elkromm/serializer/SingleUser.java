package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleUser extends SingleCredential
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        return ElkrommFactory.getFactory().getUserSerializer();
    }
}
