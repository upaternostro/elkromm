package org.paternostro.elkromm.serializer;

import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;

/**
 * {@link org.paternostro.elkromm.dto.SMSs} serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: {@link ElkrommFacade#MAX_SMS} instances of {@link org.paternostro.elkromm.dto.SMS},
 * each containing:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0x00-0x27</td><td>Message 1</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_BURLGAR} (burglary), see {@link SMS}</td></tr>
 *  <tr><td>0x28-0x4f</td><td>Message 2</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_TECHNICAL_ALARM_1}</td></tr>
 *  <tr><td>0x50-0x77</td><td>Message 3</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_TECHNICAL_ALARM_2}</td></tr>
 *  <tr><td>0x78-0x9f</td><td>Message 4</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_TECHNICAL_ALARM_3}</td></tr>
 *  <tr><td>0xa0-0xc7</td><td>Message 5</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_FIRE}</td></tr>
 *  <tr><td>0xc8-0xef</td><td>Message 6</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_PARTITION_ON}</td></tr>
 *  <tr><td>0xf0-0x117</td><td>Message 7</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_PARTITION_OFF}</td></tr>
 *  <tr><td>0x118-0x13f</td><td>Message 8</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_TAMPERING}</td></tr>
 *  <tr><td>0x140-0x167</td><td>Message 9</td><td>{@link org.paternostro.elkromm.dto.SMSs.SMSIndex#SMS_NOTICE}</td></tr>
 *  <tr><td>0x168-0x16b</td><td>Block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see SMS
 */
public class SMSs implements ElkrommSerializer<org.paternostro.elkromm.dto.SMSs>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = ElkrommFacade.MAX_SMS*SMS.PAYLOAD_SIZE + ElkrommUtils.CHECKSUM_SIZE;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SMSs obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                              data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();

        Arrays.fill(data, 0, data.length - ElkrommUtils.CHECKSUM_SIZE, (byte)0xff); // Pad with 0xff bytes
        
        for (int i = 0; i < obj.getSMSs().length; i++) {
            System.arraycopy(smsSerializer.serialize(obj.getSMSs()[i]), 0, data, i * SMS.PAYLOAD_SIZE, SMS.PAYLOAD_SIZE);
        }

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SMSs deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        org.paternostro.elkromm.dto.SMS[]                   sMSs = new org.paternostro.elkromm.dto.SMS[ElkrommFacade.MAX_SMS];
        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();
        byte[]                                              smsData = new byte[SMS.PAYLOAD_SIZE];

        for (int i = 0; i < ElkrommFacade.MAX_SMS; i++) {
            System.arraycopy(data, i * SMS.PAYLOAD_SIZE, smsData, 0, SMS.PAYLOAD_SIZE);
            sMSs[i] = smsSerializer.deserialize(smsData);
        }

        return new org.paternostro.elkromm.dto.SMSs(sMSs);
    }
}
