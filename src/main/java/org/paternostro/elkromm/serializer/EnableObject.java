package org.paternostro.elkromm.serializer;

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
