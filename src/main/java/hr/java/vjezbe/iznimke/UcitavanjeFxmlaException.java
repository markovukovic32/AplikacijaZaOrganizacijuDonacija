package hr.java.vjezbe.iznimke;

public class UcitavanjeFxmlaException extends RuntimeException{
    public UcitavanjeFxmlaException() {
    }

    public UcitavanjeFxmlaException(String message) {
        super(message);
    }

    public UcitavanjeFxmlaException(String message, Throwable cause) {
        super(message, cause);
    }

    public UcitavanjeFxmlaException(Throwable cause) {
        super(cause);
    }

    public UcitavanjeFxmlaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
