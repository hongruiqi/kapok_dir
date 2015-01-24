package cn.edu.scut.kapok.distributed.querier.search.exception;

public class QueryException extends Exception {
    public QueryException() {
    }

    public QueryException(String s) {
        super(s);
    }

    public QueryException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public QueryException(Throwable throwable) {
        super(throwable);
    }

    public QueryException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
