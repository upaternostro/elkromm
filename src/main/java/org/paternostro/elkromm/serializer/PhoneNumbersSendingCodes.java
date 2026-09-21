package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.PhoneNumber.Event;

/**
 * {@link org.paternostro.elkromm.dto.PhoneNumbersSendingCodes} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <b>Note:</b> event's primary offsets are defined by mean of {@link Event#getOffset()}; <b>Constant</b> column only shows dupes
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x10</td><td>Phone number 1</td><td>First phone number, see {@link PhoneNumber}</td><td></td></tr>
 *  <tr><td>0x11-0x21</td><td>Phone number 2</td><td>Second phone number</td><td></td></tr>
 *  <tr><td>...</td><td></td><td></td><td></td></tr>
 *  <tr><td>0xbb-0xcb</td><td>Phone number 12</td><td>Twelfth phone number</td><td></td></tr>
 *  <tr><td>0xcc</td><td>Tampering</td><td>{@link Event#PNSCE_TAMPERING}; the serializer also writes the same value at {@code 0x104} and {@code 0x170} (mirrors, not distinct events)</td><td>{@link #TAMPERING_MIRROR_1_OFFSET}, {@link #TAMPERING_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0xd4</td><td>Low battery</td><td>{@link Event#PNSCE_LOW_BATTERY}</td></tr>
 *  <tr><td>0xd8</td><td>Mains power event</td><td>{@link Event#PNSCE_MAINS_POWER}</td></tr>
 *  <tr><td>0xe4</td><td>Burglary alarm</td><td>{@link Event#PNSCE_BURGLAR_ALARM}; the serializer also writes the same value at {@code 0xe8}, {@code 0xec} and {@code 0xf0} (mirrors, not distinct events)</td><td>{@link #BURGLAR_ALARM_MIRROR_1_OFFSET}, {@link #BURGLAR_ALARM_MIRROR_2_OFFSET}, {@link #BURGLAR_ALARM_MIRROR_3_OFFSET}</td></tr>
 *  <tr><td>0xf4</td><td>Pre-alarm</td><td>{@link Event#PNSCE_PRE_ALARM}</td></tr>
 *  <tr><td>0x108</td><td>Panic</td><td>{@link Event#PNSCE_PANIC}</td></tr>
 *  <tr><td>0x10c</td><td>Silent panic</td><td>{@link Event#PNSCE_SILENT_PANIC}</td></tr>
 *  <tr><td>0x110</td><td>Fire alarm</td><td>{@link Event#PNSCE_FIRE_ALARM}</td></tr>
 *  <tr><td>0x120</td><td>Medical emergency</td><td>{@link Event#PNSCE_MEDICAL_EMERGENCY}</td></tr>
 *  <tr><td>0x124</td><td>System fault</td><td>{@link Event#PNSCE_SYSTEM_FAULT}; the serializer also writes the same value at {@code 0x130} (mirror, not distinct events)</td><td>{@link #SYSTEM_FAULT_MIRROR_OFFSET}</td></tr>
 *  <tr><td>0x134</td><td>Partition/system armed or disarmed</td><td>{@link Event#PNSCE_PARTITIONS_SYSTEM_ON_OFF}; the serializer also writes the same value at {@code 0x138} and {@code 0x140} (mirrors, not distinct events)</td><td>{@link #SYSTEM_ON_OFF_MIRROR_1_OFFSET}, {@link #SYSTEM_ON_OFF_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0x13c</td><td>Hold-up</td><td>{@link Event#PNSCE_HOLD_UP}</td></tr>
 *  <tr><td>0x144</td><td>Input excluded or re-included</td><td>{@link Event#PNSCE_INPUT_INCLUSION_EXCLUSION}; the serializer also writes the same value at {@code 0x148} (mirror, not distinct events)</td><td>{@link #INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET}</td></tr>
 *  <tr><td>0x150</td><td>Maintenance in progress</td><td>{@link Event#PNSCE_MAINTENANCE}</td></tr>
 *  <tr><td>0x154</td><td>False code entered</td><td>{@link Event#PNSCE_FALSE_CODE}</td></tr>
 *  <tr><td>0x158</td><td>Generic notice</td><td>{@link Event#PNSCE_NOTICES}</td></tr>
 *  <tr><td>0x164</td><td>Technical alarm, first type</td><td>{@link Event#PNSCE_TECHNOLOGICAL_ALARM_TYPE_1}</td></tr>
 *  <tr><td>0x168</td><td>Technical alarm, second type</td><td>{@link Event#PNSCE_TECHNOLOGICAL_ALARM_TYPE_2}</td></tr>
 *  <tr><td>0x16c</td><td>Technical alarm, third type</td><td>{@link Event#PNSCE_TECHNOLOGICAL_ALARM_TYPE_3}</td></tr>
 * </table>
 * <p>
 * <b>Event assignment table</b>: after the 12 phone records (204 bytes) comes, at the absolute offsets returned by {@link org.paternostro.elkromm.dto.PhoneNumber.Event#getOffset()}, a 
 * <b>2-byte word</b> for each reportable event:
 * <ul>
 *  <li>The word is a <b>bitmask of the phones</b> assigned to that event (bit <i>i</i> = phone <i>i+1</i>), confirmed empirically: a capture with only phone 1 enabled on an event → 
 *      {@code 0x0001}; same event with phone 1 <b>and</b> 2 → {@code 0x0003}</li>
 *  <li>The offsets are <b>not consecutive/ordered</b> as in the enum: they're scattered across the remaining ~200 bytes of the payload (between the end of the phone records and the checksum), 
 *      with large unused stretches between one event and the next — consistent with the long {@code 00 00 00 00...} sequences observed in the dumps</li>
 *  <li>The serializer also <b>duplicates</b> some events' value across multiple offsets at once (mirroring, as already seen for {@link C200bParameters}): {@code PNSCE_BURGLAR_ALARM} &rarr; 
 *      also {@code 0x00e8}, {@code 0x00ec}, {@code 0x00f0}; {@code PNSCE_INPUT_INCLUSION_EXCLUSION} &rarr; also {@code 0x0148}; {@code PNSCE_TAMPERING} &rarr; also {@code 0x0104}, 
 *      {@code 0x0170}; {@code PNSCE_SYSTEM_FAULT} &rarr; also {@code 0x0130}; {@code PNSCE_PARTITIONS_SYSTEM_ON_OFF} &rarr; primary offset {@code 0x0134} $rarr; also {@code 0x0138}, {@code 0x0140}
 *      (not just {@code 0x0140} as in a previous version of this serializer — a mirroring bug found and fixed thanks to the round-trip test suite)</li>
 * </ul>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see PhoneNumber
 */
public class PhoneNumbersSendingCodes implements ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 408;

    /** Offset of a copy of the burglary alarm assignment word, not a distinct event */
    public static final int BURGLAR_ALARM_MIRROR_1_OFFSET           = 0xe8;

    /** Offset of a copy of the burglary alarm assignment word, not a distinct event */
    public static final int BURGLAR_ALARM_MIRROR_2_OFFSET           = 0xec;

    /** Offset of a copy of the burglary alarm assignment word, not a distinct event */
    public static final int BURGLAR_ALARM_MIRROR_3_OFFSET           = 0xf0;

    /** Offset of a copy of the system arm/disarm assignment word, not a distinct event */
    public static final int SYSTEM_ON_OFF_MIRROR_1_OFFSET           = 0x138;

    /** Offset of a copy of the system arm/disarm assignment word, not a distinct event */
    public static final int SYSTEM_ON_OFF_MIRROR_2_OFFSET           = 0x140;

    /** Offset of a copy of the input exclusion/inclusion assignment word, not a distinct event */
    public static final int INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET = 0x148;

    /** Offset of a copy of the tampering assignment word, not a distinct event */
    public static final int TAMPERING_MIRROR_1_OFFSET               = 0x104;

    /** Offset of a copy of the tampering assignment word, not a distinct event */
    public static final int TAMPERING_MIRROR_2_OFFSET               = 0x170;

    /** Offset of a copy of the system fault assignment word, not a distinct event */
    public static final int SYSTEM_FAULT_MIRROR_OFFSET              = 0x130;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PhoneNumbersSendingCodes obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                                                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>  pnSerializer = ElkrommFactory.getFactory().getPhoneNumberSerializer();

        for (int i = 0; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            System.arraycopy(pnSerializer.serialize(obj.getPhoneNumbers()[i]), 0, data, i * PhoneNumber.PAYLOAD_SIZE, PhoneNumber.PAYLOAD_SIZE);
        }

        int value;

        for (Event pivot : Event.values()) {
            ElkrommUtils.setWord(data, pivot.getOffset(), value = obj.getAssignedEventMask(pivot));

            switch (pivot) {
                case PNSCE_BURGLAR_ALARM: // prima occorrenza, poi e8, ec, f0
                    ElkrommUtils.setWord(data, BURGLAR_ALARM_MIRROR_1_OFFSET, value);
                    ElkrommUtils.setWord(data, BURGLAR_ALARM_MIRROR_2_OFFSET, value);
                    ElkrommUtils.setWord(data, BURGLAR_ALARM_MIRROR_3_OFFSET, value);
                    break;
                case PNSCE_PARTITIONS_SYSTEM_ON_OFF: // prima occorrenza, poi 138, 140
                    ElkrommUtils.setWord(data, SYSTEM_ON_OFF_MIRROR_1_OFFSET, value);
                    ElkrommUtils.setWord(data, SYSTEM_ON_OFF_MIRROR_2_OFFSET, value);
                    break;
                case PNSCE_INPUT_INCLUSION_EXCLUSION: // prima occorrenza, poi 148
                    ElkrommUtils.setWord(data, INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET, value);
                    break;
                case PNSCE_TAMPERING: // prima occorrenza, poi 104, 170
                    ElkrommUtils.setWord(data, TAMPERING_MIRROR_1_OFFSET, value);
                    ElkrommUtils.setWord(data, TAMPERING_MIRROR_2_OFFSET, value);
                    break;
                case PNSCE_SYSTEM_FAULT: // prima occorrenza, poi 130
                    ElkrommUtils.setWord(data, SYSTEM_FAULT_MIRROR_OFFSET, value);
                    break;
                default:
                    break;
            }
        }

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PhoneNumbersSendingCodes deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        org.paternostro.elkromm.dto.PhoneNumber[]                   phoneNumbers = new org.paternostro.elkromm.dto.PhoneNumber[ElkrommFacade.MAX_PHONE_NUMBERS];
        int                                                         offset;
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>  pnSerializer = ElkrommFactory.getFactory().getPhoneNumberSerializer();
        byte[]                                                      pnData = new byte[PhoneNumber.PAYLOAD_SIZE];

        for (int i = 0; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            offset = i * PhoneNumber.PAYLOAD_SIZE;

            System.arraycopy(data, offset, pnData, 0, PhoneNumber.PAYLOAD_SIZE);
            phoneNumbers[i] = pnSerializer.deserialize(pnData);
        }
        
        int     value;
        int     mask;
        Event[] events = new Event[1];

        for (Event pivot : Event.values()) {
            events[0] = pivot;
            value = ElkrommUtils.getWord(data, pivot.getOffset());
            mask = 0x0001;

            for (int j = 0; j < ElkrommFacade.MAX_PHONE_NUMBERS; j++) {
                if ((value & mask) != 0) phoneNumbers[j].addAssignedEvents(events);
                mask <<= 1;
            }
        }

        return new org.paternostro.elkromm.dto.PhoneNumbersSendingCodes(phoneNumbers);
    }
}
