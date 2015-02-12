package cn.edu.scut.kapok.distributed.querier.spi.search.fetch;

public class FetchException extends Exception {
    public FetchException() {
    }

    public FetchException(String message) {
        super(message);
    }

    public FetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public FetchException(Throwable cause) {
        super(cause);
    }

    public FetchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
