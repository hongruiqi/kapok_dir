package cn.edu.scut.kapok.distributed.worker.fetch;

public class FetchException extends Exception {
    public FetchException() {
    }

    public FetchException(String s) {
        super(s);
    }

    public FetchException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FetchException(Throwable throwable) {
        super(throwable);
    }

    public FetchException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
