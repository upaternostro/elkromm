package org.paternostro.elkromm;

public class ElkrommException extends Exception {
    public ElkrommException() {
    }

    public ElkrommException(String message) {
        super(message);
    }

    public ElkrommException(Throwable cause) {
        super(cause);
    }

    public ElkrommException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElkrommException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
