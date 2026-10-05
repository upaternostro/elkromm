package org.paternostro.elkromm.emulator;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.paternostro.elkromm.dto.C200bParameters;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ConfigTest {
    private static final String SEQUENCE_HEX = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031";

    private static byte[] sequence()
    {
        byte[]  retval = new byte[C200bParameters.RESERVED_SIZE];

        for (int i = 0; i < retval.length; i++) {
            retval[i] = (byte)i;
        }

        return retval;
    }

    @Test
    public void testC200bReservedEmptyIsZero()
    {
        assertArrayEquals(new byte[C200bParameters.RESERVED_SIZE], Config.parseC200bReserved(""));
    }

    @Test
    public void testC200bReservedHex()
    {
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX));
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX.toUpperCase()));
    }

    @Test
    public void testC200bReservedIgnoresSeparators()
    {
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX.replaceAll("(..)", "$1 ").trim()));
        assertArrayEquals(sequence(), Config.parseC200bReserved(SEQUENCE_HEX.replaceAll("(..)", "$1:").replaceAll(":$", "")));
    }

    @Test
    public void testC200bReservedMalformedIsZero()
    {
        byte[]  zeros = new byte[C200bParameters.RESERVED_SIZE];

        assertArrayEquals(zeros, Config.parseC200bReserved(SEQUENCE_HEX.substring(2)));
        assertArrayEquals(zeros, Config.parseC200bReserved(SEQUENCE_HEX + "00"));
        assertArrayEquals(zeros, Config.parseC200bReserved("zz" + SEQUENCE_HEX.substring(2)));
    }
}
