package org.paternostro.elkromm.dto;

import org.paternostro.elkromm.ElkrommFacade;

public class ExcludeIncludeInput extends EnableObject {
    public ExcludeIncludeInput(byte inputOrdinal, boolean excluded) {
        super(inputOrdinal, excluded);
    }

    protected void checkOrdinal(byte ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_LOGICAL_INPUTS) throw new IllegalArgumentException("Wrong ordinal value, expected between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS + ", found " + ordinal);
    }
}
