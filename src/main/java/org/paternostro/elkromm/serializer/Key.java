package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.Key} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Enabling (bit 0) + Specialization (bits 2-3)</td><td>Enabling: {@link org.paternostro.elkromm.dto.Credential.Enabling} on the least significant bit. Specialization: {@link org.paternostro.elkromm.dto.Key.Specialization} via {@code (byte & 0x0C) >> 2} — 0=none, 1=change partition status, 2=access control, 3=access control limited to associated partitions</td></tr>
 *  <tr><td>1</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td></tr>
 *  <tr><td>2-25</td><td>Name</td><td>24 bytes</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link Keys}
 */
public class Key extends Credential
{
    @Override
    protected org.paternostro.elkromm.dto.Credential allocateCredential(String name, byte enabling, boolean[] associatedPartitions)
    {
        return new org.paternostro.elkromm.dto.Key(name, org.paternostro.elkromm.dto.Credential.Enabling.valueOf((byte)(enabling & 0x01)), org.paternostro.elkromm.dto.Key.Specialization.valueOf((byte)((enabling & 0x0C) >> 2)), associatedPartitions);
    }
}
