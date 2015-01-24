package cn.edu.scut.kapok.distributed.querier.search.resource.merger;

public class MergeException extends Exception {
    public MergeException() {
    }

    public MergeException(String s) {
        super(s);
    }

    public MergeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MergeException(Throwable throwable) {
        super(throwable);
    }

    public MergeException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
