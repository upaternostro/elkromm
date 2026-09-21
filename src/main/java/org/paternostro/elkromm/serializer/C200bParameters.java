package org.paternostro.elkromm.serializer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.C200bParameters.Event;

/**
 * {@link org.paternostro.elkromm.dto.C200bParameters} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <b>Note:</b> event's primary offsets are defined by means of {@link Event#getOffset()}; <b>Constant</b> column only shows duplicates.
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x31</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x32</td><td>Tampering</td><td>{@link Event#C2PE_TAMPERING}; the serializer also writes the same value at {@code 0x40} and {@code 0x5b} (mirrors, not distinct events), see {@link TAMPERING_MIRROR_1_OFFSET} and {@link TAMPERING_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0x33</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x34</td><td>Low battery</td><td>{@link Event#C2PE_LOW_BATTERY}</td></tr>
 *  <tr><td>0x35</td><td>Mains power</td><td>{@link Event#C2PE_MAINS_POWER}</td></tr>
 *  <tr><td>0x36-0x37</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x38</td><td>Burglary alarm</td><td>{@link Event#C2PE_BURGLAR_ALARM}; also mirrored at {@code 0x39}, {@code 0x3a}, {@code 0x3b}, see {@link #BURGLAR_ALARM_MIRROR_1_OFFSET}, {@link #BURGLAR_ALARM_MIRROR_2_OFFSET} and {@link #BURGLAR_ALARM_MIRROR_3_OFFSET}</td></tr>
 *  <tr><td>0x39-0x3b</td><td>(mirrors of 0x38)</td><td></td><td>{@link #BURGLAR_ALARM_MIRROR_1_OFFSET}, {@link #BURGLAR_ALARM_MIRROR_2_OFFSET}, {@link #BURGLAR_ALARM_MIRROR_3_OFFSET}</td></tr>
 *  <tr><td>0x3c</td><td>Pre-alarm</td><td>{@link Event#C2PE_PRE_ALARM}</td></tr>
 *  <tr><td>0x3d-0x3f</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x40</td><td>(mirror of 0x32, tampering)</td><td></td><td>{@link #TAMPERING_MIRROR_1_OFFSET}</td></tr>
 *  <tr><td>0x41</td><td>Panic</td><td>{@link Event#C2PE_PANIC}</td></tr>
 *  <tr><td>0x42</td><td>Silent panic</td><td>{@link Event#C2PE_SILENT_PANIC}</td></tr>
 *  <tr><td>0x43</td><td>Fire</td><td>{@link Event#C2PE_FIRE_ALARM}</td></tr>
 *  <tr><td>0x44-0x46</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x47</td><td>Medical emergency</td><td>{@link Event#C2PE_MEDICAL_EMERGENCY}</td></tr>
 *  <tr><td>0x48</td><td>System fault</td><td>{@link Event#C2PE_SYSTEM_FAULT}; also mirrored at {@code 0x4b}, see {@link #SYSTEM_FAULT_MIRROR_OFFSET}</td></tr>
 *  <tr><td>0x49</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x4a</td><td>Partition arm/disarm</td><td>{@link Event#C2PE_PARTITIONS_ON_OFF}</td></tr>
 *  <tr><td>0x4b</td><td>(mirror of 0x48, system fault)</td><td></td><td>{@link #SYSTEM_FAULT_MIRROR_OFFSET}</td></tr>
 *  <tr><td>0x4c</td><td>System arm/disarm</td><td>{@link Event#C2PE_SYSTEM_ON_OFF}; also mirrored at {@code 0x4d}, {@code 0x4f}, see {@link #SYSTEM_ON_OFF_MIRROR_1_OFFSET} and {@link #SYSTEM_ON_OFF_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0x4d</td><td>(mirror of 0x4c)</td><td></td><td>{@link #SYSTEM_ON_OFF_MIRROR_1_OFFSET}</td></tr>
 *  <tr><td>0x4e</td><td>Hold-up</td><td>{@link Event#C2PE_HOLD_UP}</td></tr>
 *  <tr><td>0x4f</td><td>(mirror of 0x4c)</td><td></td><td>{@link #SYSTEM_ON_OFF_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0x50</td><td>Input exclusion/inclusion</td><td>{@link Event#C2PE_INPUT_INCLUSION_EXCLUSION}; also mirrored at {@code 0x51}, see {@link #INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET}</td></tr>
 *  <tr><td>0x51</td><td>(mirror of 0x50)</td><td></td><td>{@link #INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET}</td></tr>
 *  <tr><td>0x52</td><td>Cyclical test call</td><td>{@link Event#C2PE_CYCLICAL_TEST_CALL}</td></tr>
 *  <tr><td>0x53</td><td>Maintenance in progress</td><td>{@link Event#C2PE_MAINTENANCE}</td></tr>
 *  <tr><td>0x54</td><td>False code</td><td>{@link Event#C2PE_FALSE_CODE}</td></tr>
 *  <tr><td>0x55-0x57</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x58</td><td>Technical alarm type 1</td><td>{@link Event#C2PE_TECHNOLOGICAL_ALARM_TYPE_1}</td></tr>
 *  <tr><td>0x59</td><td>Technical alarm type 2</td><td>{@link Event#C2PE_TECHNOLOGICAL_ALARM_TYPE_2}</td></tr>
 *  <tr><td>0x5a</td><td>Technical alarm type 3</td><td>{@link Event#C2PE_TECHNOLOGICAL_ALARM_TYPE_3}</td></tr>
 *  <tr><td>0x5b</td><td>(mirror of 0x32, tampering)</td><td></td><td>{@link #TAMPERING_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0x5c-0x63</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x64-0xa3</td><td>Input codes</td><td>64 bytes, one per logical input ({@link ElkrommFacade#MAX_LOGICAL_INPUTS}), {@code 0xff} if the input doesn't exist</td><td>{@link #INPUT_CODES_OFFSET}</td></tr>
 *  <tr><td>0xa4-0xa7</td><td>Block checksum</td><td></td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class C200bParameters implements ElkrommSerializer<org.paternostro.elkromm.dto.C200bParameters> 
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 168;

    /** Offset of a copy of the burglary alarm code, not a distinct event */
    public static final int BURGLAR_ALARM_MIRROR_1_OFFSET           = 0x39;

    /** Offset of a copy of the burglary alarm code, not a distinct event */
    public static final int BURGLAR_ALARM_MIRROR_2_OFFSET           = 0x3a;

    /** Offset of a copy of the burglary alarm code, not a distinct event */
    public static final int BURGLAR_ALARM_MIRROR_3_OFFSET           = 0x3b;

    /** Offset of a copy of the tampering code, not a distinct event */
    public static final int TAMPERING_MIRROR_1_OFFSET               = 0x40;

    /** Offset of a copy of the tampering code, not a distinct event */
    public static final int TAMPERING_MIRROR_2_OFFSET               = 0x5b;

    /** Offset of a copy of the system fault code, not a distinct event */
    public static final int SYSTEM_FAULT_MIRROR_OFFSET              = 0x4b;

    /** Offset of a copy of the system arm/disarm code, not a distinct event */
    public static final int SYSTEM_ON_OFF_MIRROR_1_OFFSET           = 0x4d;

    /** Offset of a copy of the system arm/disarm code, not a distinct event */
    public static final int SYSTEM_ON_OFF_MIRROR_2_OFFSET           = 0x4f;

    /** Offset of a copy of the input exclusion/inclusion code, not a distinct event */
    public static final int INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET = 0x51;

    /** Offset of the input codes, one byte per logical input ({@link ElkrommFacade#MAX_LOGICAL_INPUTS}) */
    public static final int INPUT_CODES_OFFSET                      = 0x64;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.C200bParameters obj) {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        for (Entry<Event,Byte> pivot : obj.getEventCodes().entrySet()) {
            data[pivot.getKey().getOffset()] = pivot.getValue();

            switch (pivot.getKey()) {
                case C2PE_BURGLAR_ALARM:
                    data[BURGLAR_ALARM_MIRROR_1_OFFSET] = pivot.getValue();
                    data[BURGLAR_ALARM_MIRROR_2_OFFSET] = pivot.getValue();
                    data[BURGLAR_ALARM_MIRROR_3_OFFSET] = pivot.getValue();
                    break;
                case C2PE_SYSTEM_ON_OFF:
                    data[SYSTEM_ON_OFF_MIRROR_1_OFFSET] = pivot.getValue();
                    data[SYSTEM_ON_OFF_MIRROR_2_OFFSET] = pivot.getValue();
                    break;
                case C2PE_INPUT_INCLUSION_EXCLUSION:
                    data[INPUT_INCLUSION_EXCLUSION_MIRROR_OFFSET] = pivot.getValue();
                    break;
                case C2PE_TAMPERING:
                    data[TAMPERING_MIRROR_1_OFFSET] = pivot.getValue();
                    data[TAMPERING_MIRROR_2_OFFSET] = pivot.getValue();
                    break;
                case C2PE_SYSTEM_FAULT:
                    data[SYSTEM_FAULT_MIRROR_OFFSET] = pivot.getValue();
                    break;
                default:
                    break;
            }
        }

        System.arraycopy(obj.getInputCodes(), 0, data, INPUT_CODES_OFFSET, ElkrommFacade.MAX_LOGICAL_INPUTS);

        data[0x33] = data[0x36] = data[0x37] = data[0x3d] = data[0x3e] = data[0x3f] = data[0x44] = data[0x45] = data[0x46] = data[0x49] =
        data[0x55] = data[0x56] = data[0x57] = data[0x5c] = data[0x5d] = data[0x5e] = data[0x5f] = data[0x60] = data[0x61] = data[0x62] = 
        data[0x63] = (byte)0xff;

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.C200bParameters deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data length");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        org.paternostro.elkromm.dto.C200bParameters retval = new org.paternostro.elkromm.dto.C200bParameters(new HashMap<>(), new byte[0]);

        for (Event pivot : Event.values()) {
            retval.setEventCode(pivot, data[pivot.getOffset()]);
        }

        for (int i = 0; i < ElkrommFacade.MAX_LOGICAL_INPUTS; i++) {
            retval.setInputCode(i, data[i + INPUT_CODES_OFFSET]);
        }

        return retval;
    }
}
