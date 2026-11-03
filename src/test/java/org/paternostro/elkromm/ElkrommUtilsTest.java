package org.paternostro.elkromm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.paternostro.elkromm.dto.C200bParameters;

public class ElkrommUtilsTest {
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
    public void testHexToBytesEmptyIsZero()
    {
        assertArrayEquals(new byte[0], ElkrommUtils.hexToBytes(""));
    }

    @Test
    public void testHexToBytesSeparatorIsZero()
    {
        byte[]  zero = new byte[0];

        assertArrayEquals(zero, ElkrommUtils.hexToBytes(" "));
        assertArrayEquals(zero, ElkrommUtils.hexToBytes("\t"));
        assertArrayEquals(zero, ElkrommUtils.hexToBytes(","));
        assertArrayEquals(zero, ElkrommUtils.hexToBytes(":"));
        assertArrayEquals(zero, ElkrommUtils.hexToBytes(" \t,:"));
    }

    @Test
    public void testHexToBytesCaseInsensitive()
    {
        assertArrayEquals(sequence(), ElkrommUtils.hexToBytes(SEQUENCE_HEX));
        assertArrayEquals(sequence(), ElkrommUtils.hexToBytes(SEQUENCE_HEX.toUpperCase()));
    }

    @Test
    public void testHexToBytesIgnoresSeparators()
    {
        assertArrayEquals(sequence(), ElkrommUtils.hexToBytes(SEQUENCE_HEX.replaceAll("(..)", "$1 ").trim()));
        assertArrayEquals(sequence(), ElkrommUtils.hexToBytes(SEQUENCE_HEX.replaceAll("(..)", "$1:").replaceAll(":$", "")));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testHexToBytesMalformed()
    {
        ElkrommUtils.hexToBytes("zz" + SEQUENCE_HEX.substring(2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testHexToBytesOdd()
    {
        ElkrommUtils.hexToBytes(SEQUENCE_HEX.substring(1));
    }

    @Test
    public void testHexToBytesRoundTrip()
    {
        assertEquals(SEQUENCE_HEX.toUpperCase(), ElkrommUtils.bytesToHex(ElkrommUtils.hexToBytes(SEQUENCE_HEX)));
    }
}
