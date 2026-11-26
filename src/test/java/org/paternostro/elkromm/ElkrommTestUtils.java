package org.paternostro.elkromm;

import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.Output;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.TimeProgrammer;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ElkrommTestUtils {
    public static void assertEquals(Input expected, Input actual)
    {
        org.junit.Assert.assertEquals(expected.getLogicNumber(),                    actual.getLogicNumber());
        org.junit.Assert.assertEquals(expected.getConfiguration(),                  actual.getConfiguration());
        org.junit.Assert.assertEquals(expected.getSpecialization(),                 actual.getSpecialization());
        org.junit.Assert.assertEquals(expected.getDelay(),                          actual.getDelay());
        org.junit.Assert.assertEquals(expected.getFlags(),                          actual.getFlags());
        org.junit.Assert.assertEquals(expected.getSensitivity(),                    actual.getSensitivity());
        org.junit.Assert.assertEquals(expected.getVideo(),                          actual.getVideo());

        for (int k = 0; k < ElkrommFacade.MAX_PARTITIONS; k++) {
            org.junit.Assert.assertEquals(expected.getAssociatedPartitions()[k],    actual.getAssociatedPartitions()[k]);
        }

        org.junit.Assert.assertEquals(expected.getName(),                           actual.getName());
    }

    public static void assertEquals(Reader expected, Reader actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);

        org.junit.Assert.assertEquals(expected.getAddress(),     actual.getAddress());
        ElkrommTestUtils.assertEquals(expected.getFirstInput(),  actual.getFirstInput());
        ElkrommTestUtils.assertEquals(expected.getSecondInput(), actual.getSecondInput());
        org.junit.Assert.assertEquals(expected.getLed1(),        actual.getLed1());
        org.junit.Assert.assertEquals(expected.getLed2(),        actual.getLed2());
        org.junit.Assert.assertEquals(expected.getLed3(),        actual.getLed3());
        org.junit.Assert.assertEquals(expected.getLed4(),        actual.getLed4());
        org.junit.Assert.assertEquals(expected.getEnablings(),   actual.getEnablings());
        org.junit.Assert.assertEquals(expected.getName(),        actual.getName());
    }

    public static void assertEquals(Reader[] expected, Reader[] actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            ElkrommTestUtils.assertEquals(expected[i], actual[i]);
        }
    }

    public static void assertEquals(Command expected, Command actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.getAction(),     actual.getAction());
        org.junit.Assert.assertEquals(expected.getObject(),     actual.getObject());
        org.junit.Assert.assertEquals(expected.getObjectType(), actual.getObjectType());
        org.junit.Assert.assertEquals(expected.getHour(),       actual.getHour());
        org.junit.Assert.assertEquals(expected.getMinute(),     actual.getMinute());
    }

    public static void assertEquals(Command[] expected, Command[] actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            ElkrommTestUtils.assertEquals(expected[i], actual[i]);
        }
    }

    public static void assertEquals(TimeProgrammer expected, TimeProgrammer actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        ElkrommTestUtils.assertEquals(expected.getWorkingDaysCommands(),    actual.getWorkingDaysCommands());
        ElkrommTestUtils.assertEquals(expected.getPreHolidayDaysCommands(), actual.getPreHolidayDaysCommands());
        ElkrommTestUtils.assertEquals(expected.getHolidayDaysCommands(),    actual.getHolidayDaysCommands());
        org.junit.Assert.assertArrayEquals(expected.getDayClasses(),        actual.getDayClasses());
    }

    public static void assertEquals(Output expected, Output actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.getLogicNumber(),                actual.getLogicNumber());
        org.junit.Assert.assertEquals(expected.getType(),                       actual.getType());
        org.junit.Assert.assertArrayEquals(expected.getAssociatedPartitions(),  actual.getAssociatedPartitions());
        org.junit.Assert.assertEquals(expected.getSpecialization(),             actual.getSpecialization());
        org.junit.Assert.assertEquals(expected.getName(),                       actual.getName());
    }

    public static void assertEquals(Expansion expected, Expansion actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);

        org.junit.Assert.assertEquals(expected.getAddress(),        actual.getAddress());
        org.junit.Assert.assertEquals(expected.getVersion(),        actual.getVersion());
        org.junit.Assert.assertEquals(expected.getName(),           actual.getName());

        org.junit.Assert.assertEquals(expected.getInputNum(),       actual.getInputNum());

        for (int i = 0; i < expected.getInputNum(); i++) {
            ElkrommTestUtils.assertEquals(expected.getInput(i),     actual.getInput(i));
        }

        org.junit.Assert.assertEquals(expected.getOutputNum(),      actual.getOutputNum());

        for (int i = 0; i < expected.getOutputNum(); i++) {
            ElkrommTestUtils.assertEquals(expected.getOutput(i),    actual.getOutput(i));
        }
    }

    public static void assertEquals(Expansion[] expected, Expansion[] actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            ElkrommTestUtils.assertEquals(expected[i], actual[i]);
        }
    }

    public static void assertEquals(Keyboard expected, Keyboard actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.getAddress(),                    actual.getAddress());
        org.junit.Assert.assertEquals(expected.getVersion(),                    actual.getVersion());
        ElkrommTestUtils.assertEquals(expected.getFirstInput(),                 actual.getFirstInput());
        ElkrommTestUtils.assertEquals(expected.getSecondInput(),                actual.getSecondInput());
        org.junit.Assert.assertEquals(expected.getEnablings(),                  actual.getEnablings());
        org.junit.Assert.assertArrayEquals(expected.getAssociatedPartitions(),  actual.getAssociatedPartitions());
        org.junit.Assert.assertEquals(expected.getAudioFeatures(),              actual.getAudioFeatures());
        org.junit.Assert.assertEquals(expected.getName(),                       actual.getName());
    }

    public static void assertEquals(Keyboard[] expected, Keyboard[] actual)
    {
        org.junit.Assert.assertNotNull(expected);
        org.junit.Assert.assertNotNull(actual);
        org.junit.Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            ElkrommTestUtils.assertEquals(expected[i], actual[i]);
        }
    }
}
