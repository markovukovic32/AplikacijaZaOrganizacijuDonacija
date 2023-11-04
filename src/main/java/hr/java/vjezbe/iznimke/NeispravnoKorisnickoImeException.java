package hr.java.vjezbe.iznimke;

public class NeispravnoKorisnickoImeException extends Exception{
    public NeispravnoKorisnickoImeException() {
    }

    public NeispravnoKorisnickoImeException(String message) {
        super(message);
    }

    public NeispravnoKorisnickoImeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeispravnoKorisnickoImeException(Throwable cause) {
        super(cause);
    }

    public NeispravnoKorisnickoImeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
