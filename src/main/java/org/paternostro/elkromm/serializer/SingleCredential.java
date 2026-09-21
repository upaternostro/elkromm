package org.paternostro.elkromm.serializer;

/**
 * Abstract {@link org.paternostro.elkromm.dto.SingleCredential} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Index</td><td>Credential index</td><td>{@link #INDEX_OFFSET}</td></tr>
 *  <tr><td>0x01-0x1a</td><td>Credential</td><td>See {@link Credential} and its subclass</td><td>{@link #CREDENTIAL_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Credential
 */
public abstract class SingleCredential implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleCredential>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = Credential.PAYLOAD_SIZE + 1;

    /** Offset of the index */
    public static final int INDEX_OFFSET      = 0x00;

    /** Offset of the credential, see {@link Credential} */
    public static final int CREDENTIAL_OFFSET = INDEX_OFFSET + 1;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleCredential obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential>   credentialSerializer = getSerializer();

        data[INDEX_OFFSET] = (byte)(obj.getIndex());
        System.arraycopy(credentialSerializer.serialize(obj.getCredential()), 0, data, CREDENTIAL_OFFSET, Credential.PAYLOAD_SIZE);

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

        System.arraycopy(data, CREDENTIAL_OFFSET, credentialData, 0, Credential.PAYLOAD_SIZE);

        return new org.paternostro.elkromm.dto.SingleCredential(data[INDEX_OFFSET], credentialSerializer.deserialize(credentialData));
    }

    protected ElkrommSerializer<org.paternostro.elkromm.dto.Credential> getSerializer()
    {
        throw new UnsupportedOperationException();
    }
}
