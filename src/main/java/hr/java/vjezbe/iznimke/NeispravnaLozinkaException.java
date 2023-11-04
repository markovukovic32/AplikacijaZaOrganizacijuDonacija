package hr.java.vjezbe.iznimke;

public class NeispravnaLozinkaException extends Exception {
    public NeispravnaLozinkaException() {
    }

    public NeispravnaLozinkaException(String message) {
        super(message);
    }

    public NeispravnaLozinkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeispravnaLozinkaException(Throwable cause) {
        super(cause);
    }

    public NeispravnaLozinkaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
