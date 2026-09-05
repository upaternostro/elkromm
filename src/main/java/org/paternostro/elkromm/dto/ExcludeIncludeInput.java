package org.paternostro.elkromm.dto;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * Command to exclude or re-include a single input from the alarm logic.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ExcludeIncludeInput extends EnableObject {
    /**
     * Creates a new exclude/include command for an input.
     *
     * @param inputOrdinal 1-based ordinal of the input
     * @param excluded {@code true} to exclude, {@code false} to include
     */
    public ExcludeIncludeInput(byte inputOrdinal, boolean excluded) {
        super(inputOrdinal, excluded);
    }

    /**
     * Validates that {@code ordinal} is a valid input ordinal.
     *
     * @param ordinal the ordinal to validate
     * @throws IllegalArgumentException if out of range
     */
    protected void checkOrdinal(byte ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_LOGICAL_INPUTS) throw new IllegalArgumentException("Wrong ordinal value, expected between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS + ", found " + ordinal);
    }
}
