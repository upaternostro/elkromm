package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Expansion}s serializer, DTOs &harr; byte array.
 * <p>
 * Payload structure: one or more {@link Expansion}, max {@link ElkrommFacade#MAX_EXPANSIONS} expansions.
 * <p>
 * <table>
 *  <tr><th>Offset (relative to the expansion)</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>1</td><td>Bus address</td><td>{@code 0x00} = central unit, {@code 0x01} = first expansion and so on</td></tr>
 *  <tr><td>2</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>3-6</td><td>Firmware version</td><td>ASCII string, e.g. {@code "0301"}</td></tr>
 *  <tr><td>7-310</td><td>8 inputs</td><td>38 bytes each, see {@link Input}</td></tr>
 *  <tr><td>311-532</td><td>6 outputs</td><td>37 bytes each, see {@link Output}. <b>WARNING:</b> actual expansions just have 3 outputs, only the one embedded in the central unit (address = 0x00) has 6 outputs</td></tr>
 *  <tr><td>533-556</td><td>Name</td><td>24 bytes</td></tr>
 *  <tr><td>557-558</td><td>?</td><td><b>Never sent by the client on write</b> (Hi-Connect always sends {@code 0x00 0x00} on {@code EXPANSION PROGRAMMING}/0x91), populated by the panel on read with content not yet identified — must be excluded from the block checksum calculation (see note below)</td></tr>7
 *  <tr><td>559...</td><td>Second expansion</td><td>The preceding fields repeat</td></tr>
 *  <tr><td>x-3,x</td><td>Checksum</td><td>Last four bytes are block checksum</td></tr>
 * </table>
 * <p>
 * <b>Note on the checksum</b>: on a real MP-508 v03.01 panel, the block checksum computed with the standard algorithm (see {@link ElkrommUtils#computeBlockChecksum}) doesn't match the one embedded by 
 * the panel, unless, for each expansion, the following are zeroed out beforehand: the 2 bytes at relative offset 557-558 described above, <b>and</b> bit {@code 0x10} of <i>every</i> input 
 * (relative offset {@code 7 + i*38 + 3} for the i-th input) — see {@link Input}. The current code applies this double correction before verifying the checksum, and throws an exception if it 
 * still doesn't match.
 * <p>
 * <b>The exact same phenomenon has been confirmed on real hardware on {@link Keyboards} too</b> (the only difference being that a single byte is enough there instead of two — see the dedicated 
 * section), reinforcing the idea that it's an architectural pattern of the panel (dynamic status "tucked into" otherwise-static configuration bytes), not a peculiarity of expansions alone. 
 * The same fix (2 trailing bytes + bit {@code 0x10} on the two onboard inputs) has been applied <b>by analogy, "on faith"</b>, to {@link Readers} too — unverifiable on the author's hardware, 
 * who owns no physical readers. For this reason, on Readers only, the checksum check remains a {@code logger.warn()} instead of an exception: if the hypothesis turned out to be wrong on some 
 * real installation, the symptom would be a log warning, not a hard failure. The "mysterious" byte in {@link AreasAndPartitions} (offset 328) might belong to the same family, but there's no per-input 
 * granularity there, so the connection remains a weak, unverified hypothesis.
 * <p>
 * What those 2 bytes at offset 557-558 of every expansion actually <b>are</b>, though, remains entirely unresolved (code comment: {@code // FIXME: single expansion checksum???}) — we only 
 * know they must be excluded from the calculation, not what they contain. It's unclear whether the general behavior is specific to v03.01 firmware or holds across other versions/models.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Input
 * @see Output
 */
public class Expansions implements ElkrommSerializer<Expansion[]>
{
    public static final Logger logger = LoggerFactory.getLogger(Expansions.class);

    @Override
    public byte[] serialize(Expansion[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * SerializersConstants.EXPANSION_SIZE + 4];
        int                         offset;       
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputSerializer();

        for (int i = 0; i < obj.length; i++) {
            offset = i * SerializersConstants.EXPANSION_SIZE;

            data[offset + 1] = (byte)(obj[i].getAddress() & 0xFF);
            ElkrommUtils.setText(data, offset + 3, obj[i].getVersion(), 4);
            ElkrommUtils.setText(data, offset + 533, obj[i].getName(), ElkrommFacade.NAME_LENGTH);

            for (int j = 0; j < obj[i].getInputNum(); j++) {
                offset = i * SerializersConstants.EXPANSION_SIZE + j * SerializersConstants.INPUT_SIZE + 7;
                System.arraycopy(iSerializer.serialize(obj[i].getInput(j)), 0, data, offset, SerializersConstants.INPUT_SIZE);
            }

            for (int j = 0; j < obj[i].getOutputNum(); j++) {
                offset = i * SerializersConstants.EXPANSION_SIZE + 8 * SerializersConstants.INPUT_SIZE + j * SerializersConstants.OUTPUT_SIZE + 7;

                if (obj[i].getOutput(j).getLogicNumber() == 0) {
                    // Unused input, skip
                    continue;
                }

                System.arraycopy(oSerializer.serialize(obj[i].getOutput(j)), 0, data, offset, SerializersConstants.OUTPUT_SIZE);
            }

            // FIXME: single expansion checksum???
        }

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public Expansion[] deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length % SerializersConstants.EXPANSION_SIZE != 4) throw new IllegalArgumentException("Wrong data size");

        /*
         * WARNING: my MP-508 v03.01 alarm computes the checksum excluding the last two bytes in each expansion
         * (that seems another checksum, but Claude and I were unable to understand the algorithm) and randomly
         * rises bit 4 (0x10) in inputs at offset 3 relative to the input start (i.e.: Sensitivity and Flags).
         * 
         * Moreover, please note that Hi-Connect software sets those values to zero, both the pseudo checksum and
         * the bit.
         * 
         * Assuming that those valueas are meaningless, to make checksum match, we reset last two bytes of each 
         * expansion and that bit in each input. This explains the black magic code that follows.
         * 
         * NOTICE: input's bit 4 (0x10) at offset 3 is raised by the control panel on input exclusion, but is not
         * computed in the block checksum. Please note that 0x10 is exactly {@link ElkrommFacade.InputStatus.IS_EXCLUDED}
         */
        int patchOffset = 0;
        while ((patchOffset += SerializersConstants.EXPANSION_SIZE - 2) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - 4) {
            for (int j = 0; j < 8; j++) {
                data[patchOffset + 7 + j * SerializersConstants.INPUT_SIZE + 3] &= ~0x10;
            }
            patchOffset += SerializersConstants.EXPANSION_SIZE;
        }

        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Expansion[]                 retval = new Expansion[(data.length - 4) / SerializersConstants.EXPANSION_SIZE];
        int                         offset;
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[SerializersConstants.INPUT_SIZE];
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputSerializer();
        byte[]                      oData = new byte[SerializersConstants.OUTPUT_SIZE];

        for (int i = 0; i < data.length / SerializersConstants.EXPANSION_SIZE; i++) {
            offset = i * SerializersConstants.EXPANSION_SIZE;
            retval[i] = new Expansion(data[offset + 1], ElkrommUtils.getText(data, offset + 3, 4), ElkrommUtils.getText(data, offset + 533, ElkrommFacade.NAME_LENGTH));

            for (int j = 0; j < 8; j++) {
                offset = i * SerializersConstants.EXPANSION_SIZE + j * SerializersConstants.INPUT_SIZE + 7;
                System.arraycopy(data, offset, iData, 0, SerializersConstants.INPUT_SIZE);
                retval[i].addInput(iSerializer.deserialize(iData));
            }

            for (int j = 0; j < 6; j++) {
                offset = i * SerializersConstants.EXPANSION_SIZE + 8 * SerializersConstants.INPUT_SIZE + j * SerializersConstants.OUTPUT_SIZE + 7;

                if (data[offset] == 0x00) {
                    // Unused output, skip
                    continue;
                }

                System.arraycopy(data, offset, oData, 0, SerializersConstants.OUTPUT_SIZE);
                retval[i].addOutput(oSerializer.deserialize(oData));
            }
        }

        return retval;
    }

    @Override
    public int length()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'length'");
    }
}
