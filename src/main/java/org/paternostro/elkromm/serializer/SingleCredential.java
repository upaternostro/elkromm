package org.paternostro.elkromm.serializer;

/**
 * Abstract {@link org.paternostro.elkromm.dto.SingleCredential} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Index</td><td>Credential index</td></tr>
 *  <tr><td>1-26</td><td>Credential</td><td>See {@link Credential} and its subclass</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Credential
 */
public abstract class SingleCredential implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleCredential>
{
    public static final int PAYLOAD_SIZE = Credential.PAYLOAD_SIZE + 1;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleCredential obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   credentialSerializer = getSerializer();

        data[0] = (byte)(obj.getIndex());
        System.arraycopy(credentialSerializer.serialize(obj.getCredential()), 0, data, 1, Credential.PAYLOAD_SIZE);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SingleCredential deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   credentialSerializer = getSerializer();
        byte[]                                                      credentialData = new byte[Credential.PAYLOAD_SIZE];

        System.arraycopy(data, 1, credentialData, 0, Credential.PAYLOAD_SIZE);

        return new org.paternostro.elkromm.dto.SingleCredential(data[0], credentialSerializer.deserialize(credentialData));
    }

    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        throw new UnsupportedOperationException();
    }
}
