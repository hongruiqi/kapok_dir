package cn.edu.scut.kapok.distributed.querier.api.search;

import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class WorkerAndQueryResponse {

    private final WorkerInfo worker;
    private final Optional<QueryResponse> queryResponse;

    public WorkerAndQueryResponse(WorkerInfo worker, Optional<QueryResponse> queryResponse) {
        this.worker = checkNotNull(worker);
        this.queryResponse = queryResponse;
    }

    public WorkerInfo getWorker() {
        return worker;
    }

    public Optional<QueryResponse> getQueryResponse() {
        return queryResponse;
    }
}
