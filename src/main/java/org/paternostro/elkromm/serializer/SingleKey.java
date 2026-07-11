package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

public class SingleKey extends SingleCredential
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        return ElkrommFactory.getFactory().getKeySerializer();
    }
}
