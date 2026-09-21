package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.SMSs.SMSIndex;

/**
 * {@link org.paternostro.elkromm.dto.SingleSMS} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Index</td><td><b>1-based</b> ({@code SMSIndex.ordinal() + 1}): 1=burglar, 2-4=tech.alarm 1-3, 5=fire, 6=partition on, 7=partition off, 8=tampering, 9=notice</td><td>{@link #INDEX_OFFSET}</td></tr>
 *  <tr><td>0x01-0x28</td><td>SMS</td><td>See {@link SMS}</td><td>{@link #SMS_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see SMS
 */
public class SingleSMS implements ElkrommSerializer<org.paternostro.elkromm.dto.SingleSMS>
{
    public static final int PAYLOAD_SIZE = SMS.PAYLOAD_SIZE + 1;

    /** Offset of the index */
    public static final int INDEX_OFFSET = 0x00;

    /** Offset of the SMS, see {@link SMS} */
    public static final int SMS_OFFSET   = INDEX_OFFSET + 1;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.SingleSMS obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                              data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();

        data[INDEX_OFFSET] = (byte)(obj.getIndex().ordinal() + 1);
        System.arraycopy(smsSerializer.serialize(obj.getSMS()), 0, data, SMS_OFFSET, SMS.PAYLOAD_SIZE);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.SingleSMS deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<org.paternostro.elkromm.dto.SMS>  smsSerializer = ElkrommFactory.getFactory().getSMSSerializer();
        byte[]                                              smsData = new byte[SMS.PAYLOAD_SIZE];

        System.arraycopy(data, SMS_OFFSET, smsData, 0, SMS.PAYLOAD_SIZE);
        org.paternostro.elkromm.dto.SMS sMSs = smsSerializer.deserialize(smsData);

        return new org.paternostro.elkromm.dto.SingleSMS(SMSIndex.valueOf(data[INDEX_OFFSET] - 1), sMSs);
    }
}
