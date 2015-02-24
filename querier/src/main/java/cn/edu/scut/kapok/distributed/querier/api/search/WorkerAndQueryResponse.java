package cn.edu.scut.kapok.distributed.querier.api.search;

import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;

import static com.google.common.base.Preconditions.checkNotNull;

public class WorkerAndQueryResponse {

    private final WorkerInfo worker;
    private final QueryResponse queryResponse;

    public WorkerAndQueryResponse(WorkerInfo worker, QueryResponse queryResponse) {
        this.worker = checkNotNull(worker);
        this.queryResponse = queryResponse;
    }

    public WorkerInfo getWorker() {
        return worker;
    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }
}
