package cn.edu.scut.kapok.distributed.querier.search;

public class SearchException extends Exception {
    public SearchException() {
    }

    public SearchException(String s) {
        super(s);
    }

    public SearchException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SearchException(Throwable throwable) {
        super(throwable);
    }

    public SearchException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
