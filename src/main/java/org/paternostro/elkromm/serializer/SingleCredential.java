package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;

public abstract class SingleCredential implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleCredential>
{
    public static final int CREDENTIAL_SIZE = 1+1+ElkrommFacade.NAME_LENGTH;  // 2 byte di flag e 24 di nome

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleCredential obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                      data = new byte[length()];
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   credentialSerializer = getSerializer();

        data[0] = (byte)(obj.getIndex());
        System.arraycopy(credentialSerializer.serialize(obj.getCredential()), 0, data, 1, CREDENTIAL_SIZE);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SingleCredential deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   credentialSerializer = getSerializer();
        byte[]                                                      credentialData = new byte[CREDENTIAL_SIZE];

        System.arraycopy(data, 1, credentialData, 0, CREDENTIAL_SIZE);

        return new org.paternostro.elkromm.dto.SingleCredential(data[0], credentialSerializer.deserialize(credentialData));
    }

    @Override
    public int length()
    {
        return CREDENTIAL_SIZE + 1;
    }

    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        throw new UnsupportedOperationException();
    }
}
