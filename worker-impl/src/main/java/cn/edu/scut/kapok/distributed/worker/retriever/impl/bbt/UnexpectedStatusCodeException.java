package cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt;

import cn.edu.scut.kapok.distributed.worker.api.retriever.RetrieveException;

public class UnexpectedStatusCodeException extends RetrieveException {

    private final int code;

    public UnexpectedStatusCodeException(int code) {
        super(String.format("Retrieve failed with status code: %s.", code));
        this.code = code;
    }

    public int getStatusCode() {
        return code;
    }
}
