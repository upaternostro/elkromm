package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.Checksums} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-3</td><td>Expansions checksum</td><td>Checksum of the {@link Expansions} payload</td></tr>
 *  <tr><td>4-7</td><td>Keyboards checksum</td><td>Checksum of the {@link Keyboards} payload</td></tr>
 *  <tr><td>8-11</td><td>Readers checksum</td><td>Checksum of the {@link Readers} payload</td></tr>
 *  <tr><td>12-15</td><td>System checksum</td><td>Checksum of the {@link ParametersEnablings} payload</td></tr>
 *  <tr><td>16-19</td><td>Time programmer checksum</td><td>Checksum of the {@link TimeProgrammer} payload</td></tr>
 *  <tr><td>20-23</td><td>Areas and partitions checksum</td><td>Checksum of the {@link AreasAndPartitions} payload</td></tr>
 *  <tr><td>24-27</td><td>Phone parameters checksum</td><td>Checksum of the {@link PhoneParameters} payload</td></tr>
 *  <tr><td>28-31</td><td>Phone numbers checksum</td><td>Checksum of the {@link PhoneNumbersSendingCodes} payload</td></tr>
 *  <tr><td>32-35</td><td>C200b checksum</td><td>Checksum of the {@link C200bParameters} payload</td></tr>
 *  <tr><td>36-39</td><td>Sms checksum</td><td>Checksum of the {@link SMSs} payload</td></tr>
 *  <tr><td>40-43</td><td>PSTN GSM parameters checksum</td><td>Checksum of the {@link PSTNGSM} payload</td></tr>
 *  <tr><td>44-47</td><td>Users checksum</td><td>Checksum of the {@link Users} payload</td></tr>
 *  <tr><td>48-51</td><td>Keys checksum</td><td>Checksum of the {@link Keys} payload</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Expansions
 * @see Keyboards
 * @see Readers
 * @see ParametersEnablings
 * @see TimeProgrammer
 * @see AreasAndPartitions
 * @see PhoneParameters
 * @see PhoneNumbersSendingCodes
 * @see C200bParameters
 * @see SMSs
 * @see PSTNGSM
 * @see Users
 * @see Keys
 */
public class Checksums implements ElkrommSerializer<org.paternostro.elkromm.dto.Checksums>
{
    public static final int PAYLOAD_SIZE = 13*ElkrommUtils.CHECKSUM_SIZE;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Checksums obj) {
        byte[]  data = new byte[PAYLOAD_SIZE];

        ElkrommUtils.setLong(data,  0, obj.getNodes());
        ElkrommUtils.setLong(data,  4, obj.getKeypads());
        ElkrommUtils.setLong(data,  8, obj.getReaders());
        ElkrommUtils.setLong(data, 12, obj.getSystem());
        ElkrommUtils.setLong(data, 16, obj.getTimeProgrammer());
        ElkrommUtils.setLong(data, 20, obj.getAreasAndPartitions());
        ElkrommUtils.setLong(data, 24, obj.getTelephoneParameters());
        ElkrommUtils.setLong(data, 28, obj.getTelephoneNumbers());
        ElkrommUtils.setLong(data, 32, obj.getEvents());
        ElkrommUtils.setLong(data, 36, obj.getSms());
        ElkrommUtils.setLong(data, 40, obj.getPstnGsm());
        ElkrommUtils.setLong(data, 44, obj.getUsers());
        ElkrommUtils.setLong(data, 48, obj.getKeys());

        // no checksum here

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Checksums deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data length");
        // no checksum here

        return new org.paternostro.elkromm.dto.Checksums(
            ElkrommUtils.getLong(data,  0),
            ElkrommUtils.getLong(data,  4),
            ElkrommUtils.getLong(data,  8),
            ElkrommUtils.getLong(data, 12),
            ElkrommUtils.getLong(data, 16),
            ElkrommUtils.getLong(data, 20),
            ElkrommUtils.getLong(data, 24),
            ElkrommUtils.getLong(data, 28),
            ElkrommUtils.getLong(data, 32),
            ElkrommUtils.getLong(data, 36),
            ElkrommUtils.getLong(data, 40),
            ElkrommUtils.getLong(data, 44),
            ElkrommUtils.getLong(data, 48)
        );
    }
}
