/**
 * A standalone Elkron panel emulator, speaking the same on-the-wire protocol as a real
 * panel. {@link org.paternostro.elkromm.emulator.Emulator} is the runnable entry point
 * (also set as the library jar's {@code Main-Class}); {@link org.paternostro.elkromm.emulator.ClientConnection}
 * handles a single connected client, backed by the in-memory state in
 * {@link org.paternostro.elkromm.emulator.Model} and the configuration in
 * {@link org.paternostro.elkromm.emulator.Config}.
 * <p>
 * Used both by elkromm's own test suite and, standalone, to develop and reverse-engineer
 * against without a real panel — including pointing Hi-Connect itself at it, as described
 * in the project's README.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
package org.paternostro.elkromm.emulator;
