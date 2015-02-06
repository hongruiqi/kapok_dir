package cn.edu.scut.kapok.distributed.worker.fetch;

import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import com.google.common.util.concurrent.ListenableFuture;

public interface Fetcher {
    ListenableFuture<QueryResponse> fetch(QueryRequest request);
}
