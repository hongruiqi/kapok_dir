package cn.edu.scut.kapok.distributed.querier.api.search.resource.merger;

import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.WorkerAndQueryResponse;

import java.util.List;

public interface Merger {
    SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException;
}
