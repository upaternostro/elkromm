package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Output;

public class ExpansionsTest {
    @Test
    public void test()
    {
        Expansion[]  expansions = new Expansion[1];
        boolean[]    associatedPartitions = { false, false, false, false, false, false, false, false };

        for (int i = 0; i < expansions.length; i++) {
            expansions[i] = new Expansion(i+1, "3.14", "Expansion " + (i + 1));

            for (int j = 0; j < ElkrommFacade.MAX_INPUTS; j++) {
                associatedPartitions[j] = true;
                expansions[i].addInput(new Input(i*8+j+1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_IMMEDIATE, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, associatedPartitions, "Input " + j, null));
                associatedPartitions[j] = false;
            }

            for (int j = 0; j < ElkrommFacade.MAX_OUTPUTS; j++) {
                associatedPartitions[j] = true;
                expansions[i].addOutput(new Output(i*8+j+1, Output.Type.OT_NORMALLY_HIGH, associatedPartitions, Output.Specialization.OS_OR_TC, "Output " + j));
                associatedPartitions[j] = false;
            }
        }

        byte[]  data = ElkrommFactory.getFactory().getExpansionsSerializer().serialize(expansions);

        assert data.length == expansions.length * Expansions.EXPANSION_SIZE + 4 : "Wrong length";

        Expansion[]  expansions2 = ElkrommFactory.getFactory().getExpansionsSerializer().deserialize(data);

        assert expansions.length == expansions2.length;

        for (int i = 0; i < expansions.length; i++) {
            assert expansions[i].getAddress() == expansions2[i].getAddress() : "Expected address " + expansions[i].getAddress() + " got " + expansions2[i].getAddress();
            assert expansions[i].getVersion().equals(expansions2[i].getVersion()) : "Expected version " + expansions[i].getVersion() + " got " + expansions2[i].getVersion();
            assert expansions[i].getName().equals(expansions2[i].getName()) : "Expected name " + expansions[i].getName() + " got " + expansions2[i].getName();

            assert expansions[i].getInputNum() == expansions2[i].getInputNum() : "Expected input number " + expansions[i].getInputNum() + " got " + expansions2[i].getInputNum();
            assert expansions[i].getOutputNum() == expansions2[i].getOutputNum() : "Expected output number " + expansions[i].getOutputNum() + " got " + expansions2[i].getOutputNum();

            for (int j = 0; j < ElkrommFacade.MAX_INPUTS; j++) {
                assert expansions[i].getInput(j).getLogicNumber() == expansions2[i].getInput(j).getLogicNumber();
                assert expansions[i].getInput(j).getConfiguration() == expansions2[i].getInput(j).getConfiguration();
                assert expansions[i].getInput(j).getSpecialization() == expansions2[i].getInput(j).getSpecialization();

                for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
                    assert expansions[i].getInput(j).getAssociatedPartitions()[k] == expansions2[i].getInput(j).getAssociatedPartitions()[k];
                }

                assert expansions[i].getInput(j).getName().equals(expansions2[i].getInput(j).getName());
            }

            for (int j = 0; j < ElkrommFacade.MAX_OUTPUTS; j++) {
                assert expansions[i].getOutput(j).getLogicNumber() == expansions2[i].getOutput(j).getLogicNumber();
                assert expansions[i].getOutput(j).getType() == expansions2[i].getOutput(j).getType();
                assert expansions[i].getOutput(j).getSpecialization() == expansions2[i].getOutput(j).getSpecialization();

                for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
                    assert expansions[i].getOutput(j).getAssociatedPartitions()[k] == expansions2[i].getOutput(j).getAssociatedPartitions()[k];
                }

                assert expansions[i].getOutput(j).getAssociatedPartitions()[j];
                assert expansions[i].getOutput(j).getName().equals(expansions2[i].getOutput(j).getName());
            }
        }
    }
}
