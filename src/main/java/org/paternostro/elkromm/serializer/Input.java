package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input.Configuration;
import org.paternostro.elkromm.dto.Input.Delay;
import org.paternostro.elkromm.dto.Input.Flags;
import org.paternostro.elkromm.dto.Input.Sensitivity;
import org.paternostro.elkromm.dto.Input.Specialization;
import org.paternostro.elkromm.dto.Input.Video;

/**
 * {@link org.paternostro.elkromm.dto.Input} serializer, DTO &harr; byte array.
 * <p>
 * Shared structure (38 bytes), used within each expansion (up to 8 per expansion, {@link Expansions}) and for the two onboard inputs of each {@link Keyboard}/{@link Reader}.
 * A slot with {@code logicNumber} (offset 0x00) set to {@code 0x00} is considered unused — <b>the panel does not reliably zero the other fields in this case</b>: {@code Specialization}
 * in particular can retain the value left over from the input's last real configuration (verified: an input reconfigured from {@code DELAYED} to {@code NOT_USED} kept reporting {@code DELAYED},
 * then later changed to {@code IMMEDIATE} after that input was used again for other tests). For this reason this serializer <b>always deserializes</b> an object (even for unused slots), instead
 * of returning {@code null} as in an earlier version of the code — it's the only way to preserve round-trip fidelity, since the content can't be standardized to a fixed default. 
 * This behavior doesn't occur on {@link Output}, which the panel reliably zeroes when unused.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Input's logical number</td><td>{@code 0x00} = unused slot (see the note above on leftover content)</td><td>{@link #LOGICAL_NUMBER_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>Configuration</td><td>{@link Configuration}: 0=unused, 1=NC, 2=NO, 3=single-balanced NC, 4=double-balanced NC, 7=shock, 8=roller</td><td>{@link #CONFIGURATION_OFFSET}</td></tr>
 *  <tr><td>0x02</td><td>Specialization</td><td>{@link Specialization}: see enum, 20 values (immediate, delayed, first entry, fire, tamper, ...)</td><td>{@link #SPECIALIZATION_OFFSET}</td></tr>
 *  <tr><td>0x03</td><td>Sensitivity (high bits) + Flags (low bits) + live exclusion bit</td><td>Sensitivity: {@code 0x80}=low, {@code 0x40}=medium, {@code 0x00}=high (shock/roller only).
 *   Flags (bitmask): {@code 0x01}=exclusion enabled, {@code 0x02}=double release, {@code 0x08}=OR partitions.
 *   <b>Bit {@code 0x10} isn't handled by any static field</b>: the panel raises it when the input is <b>currently excluded</b> (live status, not configuration — matches exactly 
 *   {@link ElkrommFacade.InputStatus#IS_EXCLUDED}) and lowers it again when the input is re-included. Confirmed on real hardware in three independent contexts: expansions, keypads (on their 
 *   own onboard inputs too), and by analogy (unverified) on readers. The bit must always be excluded from the block checksum calculation (see {@link Expansions}); Hi-Connect always writes it 
 *   as zero</td><td>{@link #SENSITIVITY_FLAGS_OFFSET}</td></tr>
 *  <tr><td>0x04</td><td>Associated camera</td><td>{@link Video}: 0=none, then bitmask 0x10/0x20/0x40/0x80 for cameras 1-4</td><td>{@link #ASSOCIATED_CAMERA_OFFSET}</td></tr>
 *  <tr><td>0x05</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td><td>{@link #ASSOCIATED_PARTITIONS_OFFSET}</td></tr>
 *  <tr><td>0x06-0x1d</td><td>Name</td><td>24 bytes</td><td>{@link #NAME_OFFSET}</td></tr>
 *  <tr><td>0x1e-0x21</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x22</td><td>Delay</td><td>{@link Delay}: 0=5s, 1=10s, 2=30s, 3=60s, 4=90s, 5=5min, 6=20s</td><td>{@link #DELAY_OFFSET}</td></tr>
 *  <tr><td>0x23-0x25</td><td>?</td><td>Not mapped by any DTO field</td><td>{@link #UNKNOWN_OFFSET}</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @usedby {@link Expansions}
 * @usedby {@link Keyboard}
 * @usedby {@link Reader}
 */
public class Input implements ElkrommSerializer<org.paternostro.elkromm.dto.Input>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 38;

    /** Offset of the input's logical number */
    public static final int LOGICAL_NUMBER_OFFSET        = 0x00;

    /** Offset of the configuration */
    public static final int CONFIGURATION_OFFSET         = 0x01;

    /** Offset of the specialization */
    public static final int SPECIALIZATION_OFFSET        = 0x02;

    /** Offset of the byte holding the sensitivity (high bits), the flags (low bits) and the live exclusion bit */
    public static final int SENSITIVITY_FLAGS_OFFSET     = 0x03;

    /** Offset of the associated camera */
    public static final int ASSOCIATED_CAMERA_OFFSET     = 0x04;

    /** Offset of the associated partitions */
    public static final int ASSOCIATED_PARTITIONS_OFFSET = 0x05;

    /** Offset of the name */
    public static final int NAME_OFFSET                  = 0x06;

    /** Offset of the delay */
    public static final int DELAY_OFFSET                 = 0x22;

    /** Offset of two bytes of unknown meaning, always written as {@code 0xff} */
    public static final int UNKNOWN_OFFSET               = 0x23;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Input obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[LOGICAL_NUMBER_OFFSET] = (byte)(obj.getLogicNumber() & 0xFF);
        data[CONFIGURATION_OFFSET] = obj.getConfiguration().getValue();
        data[SPECIALIZATION_OFFSET] = obj.getSpecialization().getValue();
        data[SENSITIVITY_FLAGS_OFFSET] = (byte)(obj.getSensitivity().getValue() | obj.getFlags());
        data[ASSOCIATED_CAMERA_OFFSET] = obj.getVideo().getValue();
        data[ASSOCIATED_PARTITIONS_OFFSET] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        ElkrommUtils.setText(data, NAME_OFFSET, obj.getName(), ElkrommFacade.NAME_LENGTH);
        data[DELAY_OFFSET] = obj.getDelay().getValue();
        data[UNKNOWN_OFFSET] = data[UNKNOWN_OFFSET + 1] = (byte)0xff;
        
        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Input deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        return new org.paternostro.elkromm.dto.Input(data[LOGICAL_NUMBER_OFFSET], Configuration.valueOf(data[CONFIGURATION_OFFSET]), Specialization.valueOf(data[SPECIALIZATION_OFFSET]), Sensitivity.valueOf((byte)(data[SENSITIVITY_FLAGS_OFFSET] & ~Flags.IF_ALL.getValue())), (byte)(data[SENSITIVITY_FLAGS_OFFSET] & org.paternostro.elkromm.dto.Input.Flags.IF_ALL.getValue()), org.paternostro.elkromm.dto.Input.Video.valueOf(data[ASSOCIATED_CAMERA_OFFSET]), ElkrommUtils.unpackPartitions(data[ASSOCIATED_PARTITIONS_OFFSET]), ElkrommUtils.getText(data, NAME_OFFSET, ElkrommFacade.NAME_LENGTH), Delay.valueOf(data[DELAY_OFFSET]));
    }
}
