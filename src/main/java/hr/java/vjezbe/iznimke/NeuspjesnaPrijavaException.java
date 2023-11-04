package hr.java.vjezbe.iznimke;

public class NeuspjesnaPrijavaException extends Exception{
    public NeuspjesnaPrijavaException() {
    }

    public NeuspjesnaPrijavaException(String message) {
        super(message);
    }

    public NeuspjesnaPrijavaException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeuspjesnaPrijavaException(Throwable cause) {
        super(cause);
    }

    public NeuspjesnaPrijavaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
