package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

public class SingleKeyboard implements Serializable {
    private byte        index;
    private Keyboard    keyboard
    ;

    public SingleKeyboard(byte index, Keyboard keyboard) {
        setIndex(index);
        setKeyboard(keyboard);
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        if (index < 1 || index > ElkrommFacade.MAX_KEYPADS) throw new IllegalArgumentException("Wrong index " + index + ", expected between 1 and " + ElkrommFacade.MAX_KEYPADS);

        this.index = index;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public void setKeyboard(Keyboard keyboard) {
        if (keyboard == null) throw new IllegalArgumentException("Missing mandatory keyboard");

        this.keyboard = keyboard;
    }
}
