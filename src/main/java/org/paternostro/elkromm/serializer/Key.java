package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.Key} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Enabling (bit 0) + Specialization (bits 2-3)</td><td>Enabling: {@link org.paternostro.elkromm.dto.Credential.Enabling} on the least significant bit. Specialization: {@link org.paternostro.elkromm.dto.Key.Specialization} via {@code (byte & 0x0C) >> 2} — 0=none, 1=change partition status, 2=access control, 3=access control limited to associated partitions</td><td>{@link Credential#ENABLING_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td><td>{@link Credential#ASSOCIATED_PARTITIONS_OFFSET}</td></tr>
 *  <tr><td>0x02-0x19</td><td>Name</td><td>24 bytes</td><td>{@link Credential#NAME_OFFSET}</td></tr>
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
