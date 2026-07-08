package org.paternostro.elkromm.dto;

public class EnableDisableUser extends EnableObject {
    public EnableDisableUser(byte userOrdinal, boolean enabled) {
        super(userOrdinal, enabled);
    }

    protected void checkOrdinal(byte ordinal) {
        if (ordinal < 1 || ordinal > 32) throw new IllegalArgumentException("Wrong ordinal value, expected between 1 and 32, found " + ordinal);
    }
}
