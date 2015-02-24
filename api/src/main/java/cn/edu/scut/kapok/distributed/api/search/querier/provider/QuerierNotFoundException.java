package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.api.search.SearchException;

public class QuerierNotFoundException extends SearchException {
    public QuerierNotFoundException() {
    }

    public QuerierNotFoundException(String message) {
        super(message);
    }

    public QuerierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuerierNotFoundException(Throwable cause) {
        super(cause);
    }

    public QuerierNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
