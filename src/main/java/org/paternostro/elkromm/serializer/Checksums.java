package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.Checksums} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x03</td><td>Expansions checksum</td><td>Checksum of the {@link Expansions} payload</td><td>{@link #EXPANSIONS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x04-0x07</td><td>Keyboards checksum</td><td>Checksum of the {@link Keyboards} payload</td><td>{@link #KEYBOARDS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x08-0x0b</td><td>Readers checksum</td><td>Checksum of the {@link Readers} payload</td><td>{@link #READERS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x0c-0x0f</td><td>System checksum</td><td>Checksum of the {@link ParametersEnablings} payload</td><td>{@link #SYSTEM_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x10-0x13</td><td>Time programmer checksum</td><td>Checksum of the {@link TimeProgrammer} payload</td><td>{@link #TIME_PROGRAMMER_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x14-0x17</td><td>Areas and partitions checksum</td><td>Checksum of the {@link AreasAndPartitions} payload</td><td>{@link #AREAS_AND_PARTITIONS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x18-0x1b</td><td>Phone parameters checksum</td><td>Checksum of the {@link PhoneParameters} payload</td><td>{@link #PHONE_PARAMETERS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x1c-0x1f</td><td>Phone numbers checksum</td><td>Checksum of the {@link PhoneNumbersSendingCodes} payload</td><td>{@link #PHONE_NUMBERS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x20-0x23</td><td>C200b checksum</td><td>Checksum of the {@link C200bParameters} payload</td><td>{@link #C200B_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x24-0x27</td><td>Sms checksum</td><td>Checksum of the {@link SMSs} payload</td><td>{@link #SMS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x28-0x2b</td><td>PSTN GSM parameters checksum</td><td>Checksum of the {@link PSTNGSM} payload</td><td>{@link #PSTN_GSM_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x2c-0x2f</td><td>Users checksum</td><td>Checksum of the {@link Users} payload</td><td>{@link #USERS_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x30-0x33</td><td>Keys checksum</td><td>Checksum of the {@link Keys} payload</td><td>{@link #KEYS_CHECKSUM_OFFSET}</td></tr>
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
    /** Payload size */
    public static final int PAYLOAD_SIZE = 13*ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the expansions checksum */
    public static final int EXPANSIONS_CHECKSUM_OFFSET           = 0x00;

    /** Offset of the keyboards checksum */
    public static final int KEYBOARDS_CHECKSUM_OFFSET            = EXPANSIONS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the readers checksum */
    public static final int READERS_CHECKSUM_OFFSET              = KEYBOARDS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the system checksum */
    public static final int SYSTEM_CHECKSUM_OFFSET               = READERS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the time programmer checksum */
    public static final int TIME_PROGRAMMER_CHECKSUM_OFFSET      = SYSTEM_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the areas and partitions checksum */
    public static final int AREAS_AND_PARTITIONS_CHECKSUM_OFFSET = TIME_PROGRAMMER_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the phone parameters checksum */
    public static final int PHONE_PARAMETERS_CHECKSUM_OFFSET     = AREAS_AND_PARTITIONS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the phone numbers checksum */
    public static final int PHONE_NUMBERS_CHECKSUM_OFFSET        = PHONE_PARAMETERS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the C200b checksum */
    public static final int C200B_CHECKSUM_OFFSET                = PHONE_NUMBERS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the sms checksum */
    public static final int SMS_CHECKSUM_OFFSET                  = C200B_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the PSTN GSM parameters checksum */
    public static final int PSTN_GSM_CHECKSUM_OFFSET             = SMS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the users checksum */
    public static final int USERS_CHECKSUM_OFFSET                = PSTN_GSM_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    /** Offset of the keys checksum */
    public static final int KEYS_CHECKSUM_OFFSET                 = USERS_CHECKSUM_OFFSET + ElkrommUtils.CHECKSUM_SIZE;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Checksums obj) {
        byte[]  data = new byte[PAYLOAD_SIZE];

        ElkrommUtils.setLong(data, EXPANSIONS_CHECKSUM_OFFSET, obj.getNodes());
        ElkrommUtils.setLong(data, KEYBOARDS_CHECKSUM_OFFSET, obj.getKeypads());
        ElkrommUtils.setLong(data, READERS_CHECKSUM_OFFSET, obj.getReaders());
        ElkrommUtils.setLong(data, SYSTEM_CHECKSUM_OFFSET, obj.getSystem());
        ElkrommUtils.setLong(data, TIME_PROGRAMMER_CHECKSUM_OFFSET, obj.getTimeProgrammer());
        ElkrommUtils.setLong(data, AREAS_AND_PARTITIONS_CHECKSUM_OFFSET, obj.getAreasAndPartitions());
        ElkrommUtils.setLong(data, PHONE_PARAMETERS_CHECKSUM_OFFSET, obj.getTelephoneParameters());
        ElkrommUtils.setLong(data, PHONE_NUMBERS_CHECKSUM_OFFSET, obj.getTelephoneNumbers());
        ElkrommUtils.setLong(data, C200B_CHECKSUM_OFFSET, obj.getEvents());
        ElkrommUtils.setLong(data, SMS_CHECKSUM_OFFSET, obj.getSms());
        ElkrommUtils.setLong(data, PSTN_GSM_CHECKSUM_OFFSET, obj.getPstnGsm());
        ElkrommUtils.setLong(data, USERS_CHECKSUM_OFFSET, obj.getUsers());
        ElkrommUtils.setLong(data, KEYS_CHECKSUM_OFFSET, obj.getKeys());

        // no checksum here

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Checksums deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data length");
        // no checksum here

        return new org.paternostro.elkromm.dto.Checksums(
            ElkrommUtils.getLong(data, EXPANSIONS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, KEYBOARDS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, READERS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, SYSTEM_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, TIME_PROGRAMMER_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, AREAS_AND_PARTITIONS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, PHONE_PARAMETERS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, PHONE_NUMBERS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, C200B_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, SMS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, PSTN_GSM_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, USERS_CHECKSUM_OFFSET),
            ElkrommUtils.getLong(data, KEYS_CHECKSUM_OFFSET)
        );
    }
}
