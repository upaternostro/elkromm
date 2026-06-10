package org.paternostro.elkromm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ElkrommFactoryTest {
    @Test
    public void testFactory() {
        Object factory = ElkrommFactory.getFactory();

        assertNotNull(factory);
        assertTrue(factory instanceof ElkrommFactory);
    }

    @Test
    public void testFactorySingleton() {
        Object factory = ElkrommFactory.getFactory();

        assertNotNull(factory);
        assertTrue(factory instanceof ElkrommFactory);

        Object factory2 = ElkrommFactory.getFactory();

        assertNotNull(factory2);
        assertTrue(factory2 instanceof ElkrommFactory);
        assertSame(factory, factory2);
    }

    @Test
    public void testFacade() {
        ElkrommFactory factory = ElkrommFactory.getFactory();

        assertNotNull(factory);

        Object facade = factory.getFacade(null, 0, 0);

        assertNotNull(facade);
        assertTrue(facade instanceof ElkrommFacade);
    }
}
