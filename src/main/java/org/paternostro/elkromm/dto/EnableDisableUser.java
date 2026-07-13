package org.paternostro.elkromm.dto;

import org.paternostro.elkromm.ElkrommFacade;

public class EnableDisableUser extends EnableObject {
    public EnableDisableUser(byte userOrdinal, boolean enabled) {
        super(userOrdinal, enabled);
    }

    protected void checkOrdinal(byte ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong ordinal value, expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS + ", found " + ordinal);
    }
}
