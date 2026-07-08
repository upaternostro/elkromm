package org.paternostro.elkromm.dto;

public class ExcludeIncludeInput extends EnableObject {
    public ExcludeIncludeInput(byte inputOrdinal, boolean excluded) {
        super(inputOrdinal, excluded);
    }

    protected void checkOrdinal(byte ordinal) {
        if (ordinal < 1 || ordinal > 64) throw new IllegalArgumentException("Wrong ordinal value, expected between 1 and 64, found " + ordinal);
    }
}
