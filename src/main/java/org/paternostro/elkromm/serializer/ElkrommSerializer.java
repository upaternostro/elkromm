package org.paternostro.elkromm.serializer;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public interface ElkrommSerializer<T> {
    public byte[] serialize(T obj);
    public T deserialize(byte[] data);
    public int length();
}
