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
 * A slot with {@code logicNumber} (offset 0) set to {@code 0x00} is considered unused — <b>the panel does not reliably zero the other fields in this case</b>: {@code Specialization} 
 * in particular can retain the value left over from the input's last real configuration (verified: an input reconfigured from {@code DELAYED} to {@code NOT_USED} kept reporting {@code DELAYED},
 * then later changed to {@code IMMEDIATE} after that input was used again for other tests). For this reason this serializer <b>always deserializes</b> an object (even for unused slots), instead
 * of returning {@code null} as in an earlier version of the code — it's the only way to preserve round-trip fidelity, since the content can't be standardized to a fixed default. 
 * This behavior doesn't occur on {@link Output}, which the panel reliably zeroes when unused.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Input's logical number</td><td>{@code 0x00} = unused slot (see the note above on leftover content)</td></tr>
 *  <tr><td>1</td><td>Configuration</td><td>{@link Configuration}: 0=unused, 1=NC, 2=NO, 3=single-balanced NC, 4=double-balanced NC, 7=shock, 8=roller</td></tr>
 *  <tr><td>2</td><td>Specialization</td><td>{@link Specialization}: see enum, 20 values (immediate, delayed, first entry, fire, tamper, ...)</td></tr>
 *  <tr><td>3</td><td>Sensitivity (high bits) + Flags (low bits) + live exclusion bit</td><td>Sensitivity: {@code 0x80}=low, {@code 0x40}=medium, {@code 0x00}=high (shock/roller only). 
 *   Flags (bitmask): {@code 0x01}=exclusion enabled, {@code 0x02}=double release, {@code 0x08}=OR partitions.
 *   <b>Bit {@code 0x10} isn't handled by any static field</b>: the panel raises it when the input is <b>currently excluded</b> (live status, not configuration — matches exactly 
 *   {@link ElkrommFacade.InputStatus#IS_EXCLUDED}) and lowers it again when the input is re-included. Confirmed on real hardware in three independent contexts: expansions, keypads (on their 
 *   own onboard inputs too), and by analogy (unverified) on readers. The bit must always be excluded from the block checksum calculation (see {@link Expansions}); Hi-Connect always writes it 
 *   as zero</td></tr>
 *  <tr><td>4</td><td>Associated camera</td><td>{@link Video}: 0=none, then bitmask 0x10/0x20/0x40/0x80 for cameras 1-4</td></tr>
 *  <tr><td>5</td><td>Associated partitions</td><td>Bitmask, LSB = partition 1</td></tr>
 *  <tr><td>6-29</td><td>Name</td><td>24 bytes</td></tr>
 *  <tr><td>30-33</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>34</td><td>Delay</td><td>{@link Delay}: 0=5s, 1=10s, 2=30s, 3=60s, 4=90s, 5=5min, 6=20s</td></tr>
 *  <tr><td>35-37</td><td>?</td><td>Not mapped by any DTO field</td></tr>
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
    public static final int PAYLOAD_SIZE = 38;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Input obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[PAYLOAD_SIZE];

        data[0] = (byte)(obj.getLogicNumber() & 0xFF);
        data[1] = obj.getConfiguration().getValue();
        data[2] = obj.getSpecialization().getValue();
        data[3] = (byte)(obj.getSensitivity().getValue() | obj.getFlags());
        data[4] = obj.getVideo().getValue();
        data[5] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        ElkrommUtils.setText(data, 6, obj.getName(), ElkrommFacade.NAME_LENGTH);
        data[34] = obj.getDelay().getValue();
        data[35] = data[36] = (byte)0xff;
        
        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Input deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        return new org.paternostro.elkromm.dto.Input(data[0], Configuration.valueOf(data[1]), Specialization.valueOf(data[2]), Sensitivity.valueOf((byte)(data[3] & ~Flags.IF_ALL.getValue())), (byte)(data[3] & org.paternostro.elkromm.dto.Input.Flags.IF_ALL.getValue()), org.paternostro.elkromm.dto.Input.Video.valueOf(data[4]), ElkrommUtils.unpackPartitions(data[5]), ElkrommUtils.getText(data, 6, ElkrommFacade.NAME_LENGTH), Delay.valueOf(data[34]));
    }
}
