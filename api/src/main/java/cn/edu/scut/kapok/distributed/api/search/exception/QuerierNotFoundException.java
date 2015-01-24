package cn.edu.scut.kapok.distributed.api.search.exception;

public class QuerierNotFoundException extends SearchException {
    public QuerierNotFoundException() {
    }

    public QuerierNotFoundException(String s) {
        super(s);
    }

    public QuerierNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public QuerierNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public QuerierNotFoundException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
