package cn.edu.scut.kapok.distributed.worker.api.retriever;

import cn.edu.scut.kapok.distributed.protos.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import com.google.common.util.concurrent.ListenableFuture;

public interface Retriever {
    ListenableFuture<QueryResponse> retrieve(QueryRequest request);
}
