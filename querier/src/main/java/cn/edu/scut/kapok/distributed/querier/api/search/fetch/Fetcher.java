package cn.edu.scut.kapok.distributed.querier.api.search.fetch;

import cn.edu.scut.kapok.distributed.protos.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
import com.google.common.util.concurrent.ListenableFuture;

public interface Fetcher {
    ListenableFuture<QueryResponse> fetch(WorkerInfo workerInfo, QueryRequest queryRequest);
}
