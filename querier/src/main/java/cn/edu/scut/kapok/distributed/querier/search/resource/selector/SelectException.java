package cn.edu.scut.kapok.distributed.querier.search.resource.selector;

public class SelectException extends Exception {
    public SelectException() {
    }

    public SelectException(String s) {
        super(s);
    }

    public SelectException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SelectException(Throwable throwable) {
        super(throwable);
    }

    public SelectException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
