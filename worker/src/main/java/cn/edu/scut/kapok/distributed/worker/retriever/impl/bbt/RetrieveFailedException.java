package cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt;

import cn.edu.scut.kapok.distributed.worker.retriever.spi.RetrieveException;

public class RetrieveFailedException extends RetrieveException {

    public RetrieveFailedException(Throwable throwable) {
        super("Retrieve failed without response.", throwable);
    }
}
