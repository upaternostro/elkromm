package org.paternostro.elkromm.dto;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * Command to enable or disable a single user.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class EnableDisableUser extends EnableObject {
    /**
     * Creates a new enable/disable command for a user.
     *
     * @param userOrdinal 1-based ordinal of the user (0 = TECNICO/installer)
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public EnableDisableUser(byte userOrdinal, boolean enabled) {
        super(userOrdinal, enabled);
    }

    /**
     * Validates that {@code ordinal} is a valid user ordinal.
     *
     * @param ordinal the ordinal to validate
     * @throws IllegalArgumentException if out of range
     */
    protected void checkOrdinal(byte ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong ordinal value, expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS + ", found " + ordinal);
    }
}
