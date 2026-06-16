package org.paternostro.elkromm.serializer;

import org.junit.Test;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.dto.PeripheralUnits;

public class PeripheralUnitsTest {
    @Test
    public void test()
    {
        PeripheralUnits  peripheralUnits = new PeripheralUnits();

        peripheralUnits.addExpansion(1);
        peripheralUnits.addKeypad(1);
        peripheralUnits.addKeypad(2);
        peripheralUnits.addReader(1);
        peripheralUnits.addReader(2);
        peripheralUnits.addReader(3);

        byte[]  data = ElkrommFactory.getFactory().getPeripheralUnitsSerializer().serialize(peripheralUnits);

        assert data.length == 9 : "Wrong length";

        PeripheralUnits  peripheralUnits2 = ElkrommFactory.getFactory().getPeripheralUnitsSerializer().deserialize(data);

        assert peripheralUnits.getExpansionNum() == peripheralUnits2.getExpansionNum();

        for (int i = 0; i < peripheralUnits.getExpansionNum(); i++) {
            assert peripheralUnits.getExpansionAddress(i) == peripheralUnits2.getExpansionAddress(i);
        }

        assert peripheralUnits.getKeypadNum() == peripheralUnits2.getKeypadNum();

        for (int i = 0; i < peripheralUnits.getKeypadNum(); i++) {
            assert peripheralUnits.getKeypadAddress(i) == peripheralUnits2.getKeypadAddress(i);
        }
        
        assert peripheralUnits.getReaderNum() == peripheralUnits2.getReaderNum();

        for (int i = 0; i < peripheralUnits.getReaderNum(); i++) {
            assert peripheralUnits.getReaderAddress(i) == peripheralUnits2.getReaderAddress(i);
        }
    }
}
