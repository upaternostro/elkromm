/**
 * Conversion between {@link org.paternostro.elkromm.dto} objects and the raw byte
 * payloads framed by {@link org.paternostro.elkromm.packet}. Each
 * {@link org.paternostro.elkromm.serializer.ElkrommSerializer} implementation mirrors one
 * DTO, encoding/decoding its fields at the byte offsets documented in
 * {@code PROTOCOL-ITA.md}.
 * <p>
 * Serializers are looked up through {@link org.paternostro.elkromm.ElkrommFactory}, which
 * also allows overriding any of them via a classpath properties file; this package is not
 * meant to be used directly by client code.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
package org.paternostro.elkromm.serializer;
