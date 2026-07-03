package org.paternostro.elkromm.serializer;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.C200bParameters.Event;

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
}
