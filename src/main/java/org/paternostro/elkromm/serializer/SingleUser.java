package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;

public class SingleUser extends SingleCredential
{
    @Override
    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        return ElkrommFactory.getFactory().getUserSerializer();
    }
}
