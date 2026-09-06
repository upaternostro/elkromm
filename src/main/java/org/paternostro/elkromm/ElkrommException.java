package org.paternostro.elkromm;

/**
 * General-purpose checked exception for errors occurring anywhere in the
 * elkromm library: protocol errors, I/O failures, and unexpected panel
 * responses are all reported through this type.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ElkrommException extends Exception {
    /** Creates a new exception with no message or cause. */
    public ElkrommException() {
    }

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message the detail message
     */
    public ElkrommException(String message) {
        super(message);
    }

    /**
     * Creates a new exception wrapping a lower-level cause.
     *
     * @param cause the underlying cause
     */
    public ElkrommException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with both a descriptive message and an underlying cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public ElkrommException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with full control over suppression and stack trace behavior.
     *
     * @param message the detail message
     * @param cause the underlying cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public ElkrommException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
