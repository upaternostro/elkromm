package org.paternostro.elkromm.serializer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.C200bParameters.Event;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class C200bParametersTest {
    @Test
    public void test()
    {
        Map<Event,Byte> eventCodes = new HashMap<>();

        for (Event pivot : Event.values()) {
            eventCodes.put(pivot, (byte)pivot.ordinal());
        }

        byte[]          inputCodes = new byte[ElkrommFacade.MAX_LOGICAL_INPUTS];

        for (int i = 0; i < ElkrommFacade.MAX_LOGICAL_INPUTS; ) {
            inputCodes[i] = (byte)++i;
        }
        
        C200bParameters c200bParameters = new C200bParameters(eventCodes, inputCodes);
        byte[]          data = ElkrommFactory.getFactory().getC200bParametersSerializer().serialize(c200bParameters);

        assert data.length == 168 : "Wrong length";

        C200bParameters c200bParameters2 = ElkrommFactory.getFactory().getC200bParametersSerializer().deserialize(data);

        assert c200bParameters.getEventCodes().equals(c200bParameters2.getEventCodes());
        assert c200bParameters.getInputCodes().length == c200bParameters2.getInputCodes().length;

        for (int i = 0; i < c200bParameters.getInputCodes().length; i++) {
            assert c200bParameters.getInputCodes()[i] == c200bParameters2.getInputCodes()[i];
        }
    }

    private static byte[] sequence()
    {
        byte[]  retval = new byte[C200bParameters.RESERVED_SIZE];

        for (int i = 0; i < retval.length; i++) {
            retval[i] = (byte)i;
        }

        return retval;
    }

    @Test
    public void testReservedRoundTrip()
    {
        byte[]          reserved = sequence();
        C200bParameters c200bParameters = new C200bParameters(reserved, new HashMap<>(), new byte[0]);
        byte[]          data = ElkrommFactory.getFactory().getC200bParametersSerializer().serialize(c200bParameters);

        assertEquals(org.paternostro.elkromm.serializer.C200bParameters.PAYLOAD_SIZE, data.length);

        for (int i = 0; i < reserved.length; i++) {
            assertEquals("Wrong byte at offset " + i, reserved[i], data[i]);
        }

        // deserialize() also verifies the checksum, which covers the opaque block
        assertArrayEquals(reserved, ElkrommFactory.getFactory().getC200bParametersSerializer().deserialize(data).getReserved());
    }

    @Test
    public void testDefaultReservedIsZero()
    {
        C200bParameters c200bParameters = new C200bParameters(new HashMap<>(), new byte[0]);

        assertArrayEquals(new byte[C200bParameters.RESERVED_SIZE], c200bParameters.getReserved());
    }

    @Test
    public void testReservedIsCopied()
    {
        byte[]          reserved = sequence();
        C200bParameters c200bParameters = new C200bParameters(reserved, new HashMap<>(), new byte[0]);

        reserved[0] = (byte)0x55;
        c200bParameters.getReserved()[1] = (byte)0x55;

        assertArrayEquals(sequence(), c200bParameters.getReserved());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReservedNull()
    {
        new C200bParameters(null, new HashMap<>(), new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReservedWrongLength()
    {
        new C200bParameters(new byte[C200bParameters.RESERVED_SIZE - 1], new HashMap<>(), new byte[0]);
    }
}
