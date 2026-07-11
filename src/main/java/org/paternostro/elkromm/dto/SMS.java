package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

public class SMS implements Serializable {
    private String  text;

    public SMS(String text) {
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) throw new IllegalArgumentException("Missing mandatory text");
        if (text.length() > ElkrommFacade.SMS_LENGTH) throw new IllegalArgumentException("Wrong text size, max " + ElkrommFacade.SMS_LENGTH);
        
        this.text = text;
    }
}
