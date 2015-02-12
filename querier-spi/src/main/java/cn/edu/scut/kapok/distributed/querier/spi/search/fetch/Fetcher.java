package cn.edu.scut.kapok.distributed.querier.spi.search.fetch;

import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import com.google.common.util.concurrent.ListenableFuture;

public interface Fetcher {
    ListenableFuture<QueryResponse> fetch(WorkerInfo workerInfo, QueryRequest queryRequest);
}
