/**
 * Public API of the elkromm library: the {@link org.paternostro.elkromm.ElkrommFacade}
 * interface, {@link org.paternostro.elkromm.ElkrommFactory} to obtain instances of it,
 * {@link org.paternostro.elkromm.ElkronCommand} (the protocol's command codes), and
 * shared low-level utilities in {@link org.paternostro.elkromm.ElkrommUtils}.
 * <p>
 * Client code should only need to depend on this package and on
 * {@link org.paternostro.elkromm.dto}; the {@code packet}, {@code serializer}, and
 * {@code impl} packages are implementation details.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
package org.paternostro.elkromm;
