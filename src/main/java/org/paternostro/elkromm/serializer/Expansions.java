package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Output;

public class Expansions implements ElkrommSerializer<Expansion[]>
{
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
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputsSerializer();
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputsSerializer();

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
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Expansion[]                 retval = new Expansion[(data.length - 4) / EXPANSION_SIZE];
        int                         offset;
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputsSerializer();
        byte[]                      iData = new byte[INPUT_SIZE];
        ElkrommSerializer<Output>   oSerializer = ElkrommFactory.getFactory().getOutputsSerializer();
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
