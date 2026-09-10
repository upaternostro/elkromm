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
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Expansions implements ElkrommSerializer<Expansion[]>
{
    public static final Logger logger = LoggerFactory.getLogger(Expansions.class);

    public static final int EXPANSION_SIZE  = 559;
    public static final int INPUT_SIZE      = 38;
    public static final int OUTPUT_SIZE     = 37;

    @Override
    public byte[] serialize(Expansion[] obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");
        if (obj.length == 0) throw new IllegalArgumentException("Empty mandatory obj");

        byte[]                      data = new byte[obj.length * EXPANSION_SIZE + 4];
        int                         offset;       
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputSerializer();

        for (int i = 0; i < obj.length; i++) {
            offset = i * EXPANSION_SIZE;

            data[offset + 1] = (byte)(obj[i].getAddress() & 0xFF);
            ElkrommUtils.setText(data, offset + 3, obj[i].getVersion(), 4);
            ElkrommUtils.setText(data, offset + 533, obj[i].getName(), ElkrommFacade.NAME_LENGTH);

            for (int j = 0; j < obj[i].getInputNum(); j++) {
                offset = i * EXPANSION_SIZE + j * INPUT_SIZE + 7;

                if (obj[i].getInput(j).getLogicNumber() == 0) {
                    // Unused input, skip
                    continue;
                }

                System.arraycopy(iSerializer.serialize(obj[i].getInput(j)), 0, data, offset, INPUT_SIZE);
            }

            for (int j = 0; j < obj[i].getOutputNum(); j++) {
                offset = i * EXPANSION_SIZE + 8 * INPUT_SIZE + j * OUTPUT_SIZE + 7;

                if (obj[i].getOutput(j).getLogicNumber() == 0) {
                    // Unused input, skip
                    continue;
                }

                System.arraycopy(oSerializer.serialize(obj[i].getOutput(j)), 0, data, offset, OUTPUT_SIZE);
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
        if (data.length % EXPANSION_SIZE != 4) throw new IllegalArgumentException("Wrong data size");

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
        while ((patchOffset += EXPANSION_SIZE - 2) < data.length) data[patchOffset++] = data[patchOffset++] = 0x00;
        // clear not-checksummed excluded bit from inputs
        patchOffset = 0;
        while (patchOffset < data.length - 4) {
            for (int j = 0; j < 8; j++) {
                data[patchOffset + 7 + j * INPUT_SIZE + 3] &= ~0x10;
            }
            patchOffset += EXPANSION_SIZE;
        }

        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Expansion[]                 retval = new Expansion[(data.length - 4) / EXPANSION_SIZE];
        int                         offset;
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[INPUT_SIZE];
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputSerializer();
        byte[]                      oData = new byte[OUTPUT_SIZE];

        for (int i = 0; i < data.length / EXPANSION_SIZE; i++) {
            offset = i * EXPANSION_SIZE;
            retval[i] = new Expansion(data[offset + 1], ElkrommUtils.getText(data, offset + 3, 4), ElkrommUtils.getText(data, offset + 533, ElkrommFacade.NAME_LENGTH));

            for (int j = 0; j < 8; j++) {
                offset = i * EXPANSION_SIZE + j * INPUT_SIZE + 7;

                if (data[offset] == 0x00) {
                    // Unused input, skip
                    continue;
                }

                System.arraycopy(data, offset, iData, 0, INPUT_SIZE);
                retval[i].addInput(iSerializer.deserialize(iData));
            }

            for (int j = 0; j < 6; j++) {
                offset = i * EXPANSION_SIZE + 8 * INPUT_SIZE + j * OUTPUT_SIZE + 7;

                if (data[offset] == 0x00) {
                    // Unused output, skip
                    continue;
                }

                System.arraycopy(data, offset, oData, 0, OUTPUT_SIZE);
                retval[i].addOutput(oSerializer.deserialize(oData));
            }

//            int checksum = 0;
//            for (int j = 0; j < 557; j++) {
//                checksum += data[i * EXPANSION_SIZE + j];
//            }
//            checksum += ElkrommUtils.getWord(data, i * EXPANSION_SIZE + 557);
//            logger.info("Checksum: " + checksum);
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
