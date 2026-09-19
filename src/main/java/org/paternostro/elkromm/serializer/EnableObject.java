package org.paternostro.elkromm.serializer;

/**
 * Abstract {@link org.paternostro.elkromm.dto.EnableObject} ({@link org.paternostro.elkromm.dto.EnableDisableUser}/{@link org.paternostro.elkromm.dto.ExcludeIncludeInput}) serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Object index</td><td>Can index an user or an input</td></tr>
 *  <tr><td>1</td><td>Enabling flag</td><td>0 = disabled, 1 = enabled</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see EnableDisableUser
 * @see ExcludeIncludeInput
 */
public abstract class EnableObject implements ElkrommSerializer<org.paternostro.elkromm.dto.EnableObject>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.EnableObject obj) {
        byte[]  data = new byte[length()];

        data[0] = obj.getOrdinal();
        data[1] = (byte)(obj.isEnabled() ? 0x01 : 0x00);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.EnableObject deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        org.paternostro.elkromm.dto.EnableObject  retval = allocateEnabling(data[0], data[1] == 0x01);

        return retval;
    }

    protected org.paternostro.elkromm.dto.EnableObject allocateEnabling(byte ordinal, boolean enabled)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int length() {
        return 2;
    }
}
