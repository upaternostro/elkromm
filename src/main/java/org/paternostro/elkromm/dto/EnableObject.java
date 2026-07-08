package org.paternostro.elkromm.dto;

import java.io.Serializable;

public abstract class EnableObject implements Serializable {
    private byte    ordinal;
    private boolean enabled;

    public EnableObject(byte ordinal, boolean enabled) {
        setOrdinal(ordinal);
        setEnabled(enabled);
    }

    public byte getOrdinal() {
        return ordinal;
    }

    protected void checkOrdinal(byte ordinal) {
        throw new UnsupportedOperationException("Unimplemented method 'checkOrdinal'");
    }

    public void setOrdinal(byte ordinal) {
        checkOrdinal(ordinal);
        // if (!Partition.isValid(ordinal)) throw new IllegalArgumentException("Wrong partitions value");
        this.ordinal = ordinal;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
