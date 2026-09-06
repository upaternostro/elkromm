/**
 * Data model for the Elkron/Hi-Connect protocol: one class per configuration or status
 * concept (areas, partitions, users, keys, inputs, outputs, keypads, proximity readers,
 * expansions, phone numbers, PSTN/GSM, SMS messages, C200B, time programmer...),
 * independent of how each is represented on the wire.
 * <p>
 * DTOs validate their own invariants in their setters/constructors (ranges, mandatory
 * fields, valid bitmask values) and are the types exchanged through
 * {@link org.paternostro.elkromm.ElkrommFacade}. Converting these DTOs to and from raw
 * byte payloads is the job of the {@link org.paternostro.elkromm.serializer} package, not
 * of the DTOs themselves.
 * <p>
 * Naming convention: a plain class name (e.g. {@code Keyboard}) represents one instance; a
 * {@code Single}-prefixed class (e.g. {@link org.paternostro.elkromm.dto.SingleKeyboard})
 * pairs one instance with the index needed to write it individually, for the protocol's
 * single-instance write commands (see {@code PROTOCOL-ITA.md}). {@link org.paternostro.elkromm.dto.Reader}
 * is the one exception: it carries its own {@code address} field and is used directly,
 * with no {@code SingleReader} wrapper.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
package org.paternostro.elkromm.dto;
