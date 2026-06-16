package org.paternostro.elkromm.serializer;

public interface ElkrommSerializer<T> {
    public byte[] serialize(T obj);
    public T deserialize(byte[] data);
    public int length();
}
