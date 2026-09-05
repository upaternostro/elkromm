package org.paternostro.elkromm.dto;

import java.io.Serializable;

/**
 * Common base for simple "enable/disable something by ordinal" commands,
 * such as {@link EnableDisableUser} and {@link ExcludeIncludeInput}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public abstract class EnableObject implements Serializable {
    private byte    ordinal;
    private boolean enabled;

    /**
     * Creates a new enable/disable command.
     *
     * @param ordinal 1-based ordinal of the object to act on
     * @param enabled the state to set
     */
    public EnableObject(byte ordinal, boolean enabled) {
        setOrdinal(ordinal);
        setEnabled(enabled);
    }

    /**
     * Returns the 1-based ordinal of the object this command acts on.
     *
     * @return the ordinal
     */
    public byte getOrdinal() {
        return ordinal;
    }

    /**
     * Validates an ordinal against the valid range for the concrete subclass.
     * <p>
     * Subclasses must override this; the base implementation always throws.
     *
     * @param ordinal the ordinal to validate
     * @throws UnsupportedOperationException if not overridden by the subclass
     */
    protected void checkOrdinal(byte ordinal) {
        throw new UnsupportedOperationException("Unimplemented method 'checkOrdinal'");
    }

    /**
     * Sets the 1-based ordinal of the object this command acts on.
     *
     * @param ordinal the ordinal to set
     * @throws IllegalArgumentException if out of the subclass's valid range
     */
    public void setOrdinal(byte ordinal) {
        checkOrdinal(ordinal);
        this.ordinal = ordinal;
    }

    /**
     * Returns whether this command enables (as opposed to disables) the object.
     *
     * @return {@code true} if enabling
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this command enables (as opposed to disables) the object.
     *
     * @param enabled {@code true} to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{ordinal=").append(ordinal).append(", enabled=").append(enabled).append("}");

        return sb.toString();
    }
}
