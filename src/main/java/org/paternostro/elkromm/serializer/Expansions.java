package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
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

        byte[]  data = new byte[obj.length * EXPANSION_SIZE + 4];
        int     offset;       

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

                data[offset] = (byte)(obj[i].getInput(j).getLogicNumber() & 0xFF);
                data[offset + 1] = obj[i].getInput(j).getConfiguration().getValue();
                data[offset + 2] = obj[i].getInput(j).getSpecialization().getValue();
                data[offset + 5] = ElkrommUtils.packPartitions(obj[i].getInput(j).getAssociatedPartitions());
                ElkrommUtils.setText(data, offset + 6, obj[i].getInput(j).getName(), ElkrommFacade.NAME_LENGTH);
            }

            for (int j = 0; j < obj[i].getOutputNum(); j++) {
                offset = i * EXPANSION_SIZE + 8 * INPUT_SIZE + j * OUTPUT_SIZE + 7;

                if (obj[i].getOutput(j).getLogicNumber() == 0) {
                    // Unused input, skip
                    continue;
                }

                data[offset] = (byte)(obj[i].getOutput(j).getLogicNumber() & 0xFF);
                data[offset + 1] = obj[i].getOutput(j).getType().getValue();
                data[offset + 2] = ElkrommUtils.packPartitions(obj[i].getOutput(j).getAssociatedPartitions());
                data[offset + 3] = obj[i].getOutput(j).getSpecialization().getValue();
                ElkrommUtils.setText(data, offset + 8, obj[i].getOutput(j).getName(), ElkrommFacade.NAME_LENGTH);
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

        Expansion[] retval = new Expansion[(data.length - 4) / EXPANSION_SIZE];
        int         offset;

        for (int i = 0; i < data.length / EXPANSION_SIZE; i++) {
            offset = i * EXPANSION_SIZE;
            retval[i] = new Expansion(data[offset + 1], ElkrommUtils.getText(data, offset + 3, 4), ElkrommUtils.getText(data, offset + 533, ElkrommFacade.NAME_LENGTH));

            for (int j = 0; j < 8; j++) {
                offset = i * EXPANSION_SIZE + j * INPUT_SIZE + 7;

                if (data[offset] == 0x00) {
                    // Unused input, skip
                    continue;
                }

                retval[i].addInput(new Input(data[offset], Input.Configuration.toConfiguration(data[offset + 1]), Input.Specialization.toSpecialization(data[offset + 2]), ElkrommUtils.unpackPartitions(data[offset + 5]), ElkrommUtils.getText(data, offset + 6, ElkrommFacade.NAME_LENGTH)));
            }

            for (int j = 0; j < 6; j++) {
                offset = i * EXPANSION_SIZE + 8 * INPUT_SIZE + j * OUTPUT_SIZE + 7;

                if (data[offset] == 0x00) {
                    // Unused output, skip
                    continue;
                }

                retval[i].addOutput(new Output(data[offset], Output.Type.toType(data[offset + 1]), ElkrommUtils.unpackPartitions(data[offset + 2]), Output.Specialization.toSpecialization(data[offset + 3]), ElkrommUtils.getText(data, offset + 8, ElkrommFacade.NAME_LENGTH)));
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
