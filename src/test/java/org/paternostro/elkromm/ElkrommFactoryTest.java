package org.paternostro.elkromm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.paternostro.elkromm.impl.MyElkrommFacadeImpl;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
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
        assertTrue(factory instanceof MyElkrommFactory);

        Object factory2 = ElkrommFactory.getFactory();

        assertNotNull(factory2);
        assertTrue(factory2 instanceof MyElkrommFactory);
        assertSame(factory, factory2);
    }

    @Test
    public void testFacade() throws IOException {
        ElkrommFactory factory = ElkrommFactory.getFactory();

        assertNotNull(factory);

        Object facade = factory.getElkrommFacade(null, 0, 0);

        assertNotNull(facade);
        assertTrue(facade instanceof ElkrommFacade);
        assertTrue(facade instanceof MyElkrommFacadeImpl);
    }
}
