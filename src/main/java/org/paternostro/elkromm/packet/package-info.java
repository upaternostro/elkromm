/**
 * On-the-wire packet framing: one {@link org.paternostro.elkromm.packet.ElkrommPacket}
 * subclass per protocol command, handling the low-level envelope (plant code, packet
 * index, data length, checksum) around the raw payload produced by the
 * {@link org.paternostro.elkromm.serializer} package.
 * <p>
 * This package works exclusively with byte arrays; it has no knowledge of the semantic
 * data model in {@link org.paternostro.elkromm.dto} and is not part of the library's
 * public API.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
package org.paternostro.elkromm.packet;
