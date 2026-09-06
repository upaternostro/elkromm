package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A single {@link Keyboard} (keypad) configuration paired with its index,
 * for the {@code KEYPAD_PROGRAMMING} single-instance write command.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleKeyboard implements Serializable {
    private byte        index;
    private Keyboard    keyboard
    ;

    /**
     * Creates a new single-keypad write.
     *
     * @param index 1-based position of the keypad in its array
     * @param keyboard the keypad configuration
     */
    public SingleKeyboard(byte index, Keyboard keyboard) {
        setIndex(index);
        setKeyboard(keyboard);
    }

    /**
     * Returns the 1-based index of this keypad.
     *
     * @return the index
     */
    public byte getIndex() {
        return index;
    }

    /**
     * Sets the 1-based index of this keypad.
     *
     * @param index the index to set, in range [1, {@link ElkrommFacade#MAX_KEYPADS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setIndex(byte index) {
        if (index < 1 || index > ElkrommFacade.MAX_KEYPADS) throw new IllegalArgumentException("Wrong index " + index + ", expected between 1 and " + ElkrommFacade.MAX_KEYPADS);

        this.index = index;
    }

    /**
     * Returns the keypad configuration.
     *
     * @return the keypad
     */
    public Keyboard getKeyboard() {
        return keyboard;
    }

    /**
     * Sets the keypad configuration.
     *
     * @param keyboard the keypad to set, not {@code null}
     * @throws IllegalArgumentException if {@code keyboard} is {@code null}
     */
    public void setKeyboard(Keyboard keyboard) {
        if (keyboard == null) throw new IllegalArgumentException("Missing mandatory keyboard");

        this.keyboard = keyboard;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{index=").append(index).append(", keyboard=").append(keyboard).append("}");

        return sb.toString();
    }
}
