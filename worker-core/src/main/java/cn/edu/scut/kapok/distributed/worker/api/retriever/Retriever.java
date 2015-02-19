package cn.edu.scut.kapok.distributed.worker.api.retriever;

import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import com.google.common.util.concurrent.ListenableFuture;

public interface Retriever {
    ListenableFuture<QueryResponse> retrieve(QueryRequest request) throws RetrieveException;
}
