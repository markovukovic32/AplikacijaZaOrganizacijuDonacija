package hr.java.vjezbe.iznimke;

public class BazaPodatakaException extends RuntimeException{
    public BazaPodatakaException() {
    }

    public BazaPodatakaException(String message) {
        super(message);
    }

    public BazaPodatakaException(String message, Throwable cause) {
        super(message, cause);
    }

    public BazaPodatakaException(Throwable cause) {
        super(cause);
    }

    public BazaPodatakaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
