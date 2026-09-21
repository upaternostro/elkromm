package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Output;

/**
 * {@link Expansion}s serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: one or more {@link Expansion}, max {@link ElkrommFacade#MAX_EXPANSIONS} expansions.
 * <p>
 * <table>
 *  <tr><th>Offset (relative to the expansion)</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x01</td><td>Bus address</td><td>{@code 0x00} = central unit, {@code 0x01} = first expansion and so on</td><td>{@link #EXPANSION_ADDRESS_OFFSET}</td></tr>
 *  <tr><td>0x02</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x03-0x06</td><td>Firmware version</td><td>ASCII string, e.g. {@code "0301"}</td><td>{@link #EXPANSION_VERSION_OFFSET}</td></tr>
 *  <tr><td>0x07-0x136</td><td>8 inputs</td><td>38 bytes each, see {@link Input}</td><td>{@link #EXPANSION_INPUTS_OFFSET}</td></tr>
 *  <tr><td>0x137-0x214</td><td>6 outputs</td><td>37 bytes each, see {@link Output}. <b>WARNING:</b> actual expansions just have 3 outputs, only the one embedded in the central unit (address = 0x00) has 6 outputs</td><td>{@link #EXPANSION_OUTPUTS_OFFSET}</td></tr>
 *  <tr><td>0x215-0x22c</td><td>Name</td><td>24 bytes</td><td>{@link #EXPANSION_NAME_OFFSET}</td></tr>
 *  <tr><td>0x22d-0x22e</td><td>?</td><td><b>Never sent by the client on write</b> (Hi-Connect always sends {@code 0x00 0x00} on {@code EXPANSION PROGRAMMING}/0x91), populated by the panel on read with content not yet identified — must be excluded from the block checksum calculation (see note below)</td><td>{@link #EXPANSION_PSEUDO_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x22f...</td><td>Second expansion</td><td>The preceding fields repeat</td><td></td></tr>
 *  <tr><td>x-3,x</td><td>Checksum</td><td>Last four bytes are block checksum</td><td></td></tr>
 * </table>
 * <p>
 * <b>Note on the checksum</b>: on a real MP-508 v03.01 panel, the block checksum computed with the standard algorithm (see {@link ElkrommUtils#computeBlockChecksum}) doesn't match the one embedded by 
 * the panel, unless, for each expansion, the following are zeroed out beforehand: the 2 bytes at relative offset 0x22d-0x22e described above, <b>and</b> bit {@code 0x10} of <i>every</i> input
 * (relative offset {@link #EXPANSION_INPUTS_OFFSET} + i&times;{@link org.paternostro.elkromm.serializer.Input#PAYLOAD_SIZE} + {@link org.paternostro.elkromm.serializer.Input#SENSITIVITY_FLAGS_OFFSET} for the i-th input) — see {@link Input}. The current code applies this double correction before verifying the checksum, and throws an exception if it
 * still doesn't match.
 * <p>
 * <b>The exact same phenomenon has been confirmed on real hardware on {@link Keyboards} too</b> (the only difference being that a single byte is enough there instead of two — see the dedicated 
 * section), reinforcing the idea that it's an architectural pattern of the panel (dynamic status "tucked into" otherwise-static configuration bytes), not a peculiarity of expansions alone. 
 * The same fix (2 trailing bytes + bit {@code 0x10} on the two onboard inputs) has been applied <b>by analogy, "on faith"</b>, to {@link Readers} too — unverifiable on the author's hardware, 
 * who owns no physical readers. For this reason, on Readers only, the checksum check remains a {@code logger.warn()} instead of an exception: if the hypothesis turned out to be wrong on some 
 * real installation, the symptom would be a log warning, not a hard failure. The "mysterious" byte in {@link AreasAndPartitions} (offset 0x148) might belong to the same family, but there's no per-input
 * granularity there, so the connection remains a weak, unverified hypothesis.
 * <p>
 * What those 2 bytes at offset 0x22d-0x22e of every expansion actually <b>are</b>, though, remains entirely unresolved (code comment: {@code // FIXME: single expansion checksum???}) — we only
 * know they must be excluded from the calculation, not what they contain. It's unclear whether the general behavior is specific to v03.01 firmware or holds across other versions/models.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Input
 * @see Output
 */
public class Expansions implements ElkrommSerializer<Expansion[]>
{
    /** Expansion size (temporary, waiting for single Expansion serializer) */
    public static final int EXPANSION_PAYLOAD_SIZE = 559;

    /** Offset of the bus address */
    public static final int EXPANSION_ADDRESS_OFFSET         = 0x01;

    /** Offset of the firmware version, {@link ElkrommFacade#VERSION_LENGTH} bytes */
    public static final int EXPANSION_VERSION_OFFSET         = 0x03;

    /** Offset of the inputs, {@link ElkrommFacade#MAX_EXP_INPUTS} slots of {@link org.paternostro.elkromm.serializer.Input#PAYLOAD_SIZE} bytes each */
    public static final int EXPANSION_INPUTS_OFFSET          = 0x07;

    /** Offset of the outputs, {@link ElkrommFacade#MAX_PANEL_OUTPUTS} slots of {@link org.paternostro.elkromm.serializer.Output#PAYLOAD_SIZE} bytes each */
    public static final int EXPANSION_OUTPUTS_OFFSET         = EXPANSION_INPUTS_OFFSET + ElkrommFacade.MAX_EXP_INPUTS * org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE;

    /** Offset of the expansion name, {@link ElkrommFacade#NAME_LENGTH} bytes */
    public static final int EXPANSION_NAME_OFFSET            = EXPANSION_OUTPUTS_OFFSET + ElkrommFacade.MAX_PANEL_OUTPUTS * org.paternostro.elkromm.serializer.Output.PAYLOAD_SIZE;

    /** Offset of the pseudo checksum, excluded from the block checksum calculation (see the note in {@link #deserialize}) */
    public static final int EXPANSION_PSEUDO_CHECKSUM_OFFSET = EXPANSION_NAME_OFFSET + ElkrommFacade.NAME_LENGTH;

    /** Size of the pseudo checksum */
    public static final int EXPANSION_PSEUDO_CHECKSUM_SIZE   = 2;

    @Override
    public byte[] serialize(Expansion[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * EXPANSION_PAYLOAD_SIZE + ElkrommUtils.CHECKSUM_SIZE];
        int                         offset;       
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputSerializer();

        for (int i = 0; i < obj.length; i++) {
            offset = i * EXPANSION_PAYLOAD_SIZE;

            data[offset + EXPANSION_ADDRESS_OFFSET] = (byte)(obj[i].getAddress() & 0xFF);
            ElkrommUtils.setText(data, offset + EXPANSION_VERSION_OFFSET, obj[i].getVersion(), ElkrommFacade.VERSION_LENGTH);
            ElkrommUtils.setText(data, offset + EXPANSION_NAME_OFFSET, obj[i].getName(), ElkrommFacade.NAME_LENGTH);

            for (int j = 0; j < obj[i].getInputNum(); j++) {
                offset = i * EXPANSION_PAYLOAD_SIZE + j * org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE + EXPANSION_INPUTS_OFFSET;
                System.arraycopy(iSerializer.serialize(obj[i].getInput(j)), 0, data, offset, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
            }

            for (int j = 0; j < obj[i].getOutputNum(); j++) {
                offset = i * EXPANSION_PAYLOAD_SIZE + EXPANSION_OUTPUTS_OFFSET + j * org.paternostro.elkromm.serializer.Output.PAYLOAD_SIZE;

                if (obj[i].getOutput(j).getLogicNumber() == 0) {
                    // Unused input, skip
                    continue;
                }

                System.arraycopy(oSerializer.serialize(obj[i].getOutput(j)), 0, data, offset, org.paternostro.elkromm.serializer.Output.PAYLOAD_SIZE);
            }

            // FIXME: single expansion checksum???
        }

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public Expansion[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % EXPANSION_PAYLOAD_SIZE != ElkrommUtils.CHECKSUM_SIZE) throw new IllegalArgumentException("Wrong data size");

        /*
         * WARNING: my MP-508 v03.01 alarm computes the checksum excluding the last two bytes in each expansion
         * (that seems another checksum, but Claude and I were unable to understand the algorithm) and randomly
         * rises bit 4 (0x10) in inputs at offset 0x03 relative to the input start (i.e.: Sensitivity and Flags).
         * 
         * Moreover, please note that Hi-Connect software sets those values to zero, both the pseudo checksum and
         * the bit.
         * 
         * Assuming that those valueas are meaningless, to make checksum match, we reset last two bytes of each 
         * expansion and that bit in each input. This explains the black magic code that follows.
         * 
         * NOTICE: input's bit 4 (0x10) at offset 0x03 is raised by the control panel on input exclusion, but is not
         * computed in the block checksum. Please note that 0x10 is exactly {@link ElkrommFacade.InputStatus.IS_EXCLUDED}
         */
        int patchOffset = 0;
        while ((patchOffset += EXPANSION_PSEUDO_CHECKSUM_OFFSET) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - ElkrommUtils.CHECKSUM_SIZE) {
            for (int j = 0; j < ElkrommFacade.MAX_EXP_INPUTS; j++) {
                data[patchOffset + EXPANSION_INPUTS_OFFSET + j * org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE + org.paternostro.elkromm.serializer.Input.SENSITIVITY_FLAGS_OFFSET] &= ~0x10;
            }
            patchOffset += EXPANSION_PAYLOAD_SIZE;
        }

        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        Expansion[]                 retval = new Expansion[(data.length - ElkrommUtils.CHECKSUM_SIZE) / EXPANSION_PAYLOAD_SIZE];
        int                         offset;
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE];
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputSerializer();
        byte[]                      oData = new byte[org.paternostro.elkromm.serializer.Output.PAYLOAD_SIZE];

        for (int i = 0; i < data.length / EXPANSION_PAYLOAD_SIZE; i++) {
            offset = i * EXPANSION_PAYLOAD_SIZE;
            retval[i] = new Expansion(data[offset + EXPANSION_ADDRESS_OFFSET], ElkrommUtils.getText(data, offset + EXPANSION_VERSION_OFFSET, ElkrommFacade.VERSION_LENGTH), ElkrommUtils.getText(data, offset + EXPANSION_NAME_OFFSET, ElkrommFacade.NAME_LENGTH));

            for (int j = 0; j < ElkrommFacade.MAX_EXP_INPUTS; j++) {
                offset = i * EXPANSION_PAYLOAD_SIZE + j * org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE + EXPANSION_INPUTS_OFFSET;
                System.arraycopy(data, offset, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
                retval[i].addInput(iSerializer.deserialize(iData));
            }

            for (int j = 0; j < ElkrommFacade.MAX_PANEL_OUTPUTS; j++) {
                offset = i * EXPANSION_PAYLOAD_SIZE + EXPANSION_OUTPUTS_OFFSET + j * org.paternostro.elkromm.serializer.Output.PAYLOAD_SIZE;

                if (data[offset] == 0x00) {
                    // Unused output, skip
                    continue;
                }

                System.arraycopy(data, offset, oData, 0, org.paternostro.elkromm.serializer.Output.PAYLOAD_SIZE);
                retval[i].addOutput(oSerializer.deserialize(oData));
            }
        }

        return retval;
    }
}
