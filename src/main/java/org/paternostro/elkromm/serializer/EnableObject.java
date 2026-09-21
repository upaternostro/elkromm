package org.paternostro.elkromm.serializer;

/**
 * Abstract {@link org.paternostro.elkromm.dto.EnableObject} ({@link org.paternostro.elkromm.dto.EnableDisableUser}/{@link org.paternostro.elkromm.dto.ExcludeIncludeInput}) serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Object index</td><td>Can index an user or an input</td><td>{@link #OBJECT_INDEX_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Enabling flag</td><td>0 = disabled, 1 = enabled</td><td>{@link #ENABLING_FLAG_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see EnableDisableUser
 * @see ExcludeIncludeInput
 */
public abstract class EnableObject implements ElkrommSerializer<org.paternostro.elkromm.dto.EnableObject>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 2;

    /** Offset of the object index */
    public static final int OBJECT_INDEX_OFFSET  = 0x00;

    /** Offset of the enabling flag */
    public static final int ENABLING_FLAG_OFFSET = 0x01;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.EnableObject obj) {
        byte[]  data = new byte[PAYLOAD_SIZE];

        data[OBJECT_INDEX_OFFSET] = obj.getOrdinal();
        data[ENABLING_FLAG_OFFSET] = (byte)(obj.isEnabled() ? 0x01 : 0x00);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.EnableObject deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        org.paternostro.elkromm.dto.EnableObject  retval = allocateEnabling(data[OBJECT_INDEX_OFFSET], data[ENABLING_FLAG_OFFSET] == 0x01);

        return retval;
    }

    protected org.paternostro.elkromm.dto.EnableObject allocateEnabling(byte ordinal, boolean enabled)
    {
        throw new UnsupportedOperationException();
    }
}
