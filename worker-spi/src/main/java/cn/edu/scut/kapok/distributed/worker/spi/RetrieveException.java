package cn.edu.scut.kapok.distributed.worker.spi;

public class RetrieveException extends Exception {

    public RetrieveException() {
    }

    public RetrieveException(String s) {
        super(s);
    }

    public RetrieveException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RetrieveException(Throwable throwable) {
        super(throwable);
    }

    public RetrieveException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
