package cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt;

import cn.edu.scut.kapok.distributed.worker.api.retriever.RetrieveException;

/**
 * Retrieve failed without response. Maybe some error is occured in server.
 */
public class RetrieveFailedException extends RetrieveException {

    public RetrieveFailedException(Throwable throwable) {
        super("Retrieve failed without response.", throwable);
    }
}
