package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * The text of a single SMS notification message.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SMS implements Serializable {
    private String  text;

    /**
     * Creates a new SMS message.
     *
     * @param text the message text, up to {@link ElkrommFacade#SMS_LENGTH} characters
     */
    public SMS(String text) {
        setText(text);
    }

    /**
     * Returns the message text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the message text.
     *
     * @param text the text to set, not {@code null} and at most {@link ElkrommFacade#SMS_LENGTH} characters
     * @throws IllegalArgumentException if {@code text} is {@code null} or too long
     */
    public void setText(String text) {
        if (text == null) throw new IllegalArgumentException("Missing mandatory text");
        if (text.length() > ElkrommFacade.SMS_LENGTH) throw new IllegalArgumentException("Wrong text size, max " + ElkrommFacade.SMS_LENGTH);
        
        this.text = text;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{text=").append(text).append("}");

        return sb.toString();
    }
}
