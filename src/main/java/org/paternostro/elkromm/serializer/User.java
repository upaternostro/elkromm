package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.User} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Enabling</td><td>{@link org.paternostro.elkromm.dto.Credential.Enabling}: 0=disabled, 1=enabled, 2=always enabled</td><td>{@link Credential#ENABLING_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td><td>{@link Credential#ASSOCIATED_PARTITIONS_OFFSET}</td></tr>
 *  <tr><td>0x02-0x19</td><td>Name</td><td>24 bytes</td><td>{@link Credential#NAME_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 *
 * @usedby {@link SingleUser}
 * @usedby {@link Users}
 */
public class User extends Credential
{
    @Override
    protected org.paternostro.elkromm.dto.Credential allocateCredential(String name, byte enabling, boolean[] associatedPartitions)
    {
        return new org.paternostro.elkromm.dto.User(name, org.paternostro.elkromm.dto.Credential.Enabling.valueOf(enabling), associatedPartitions);
    }
}
