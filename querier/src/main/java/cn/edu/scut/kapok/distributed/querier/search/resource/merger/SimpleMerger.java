package cn.edu.scut.kapok.distributed.querier.search.resource.merger;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.search.WorkerAndQueryResponse;

import java.util.List;

public class SimpleMerger implements Merger {
    @Override
    public SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException {
        return null;
    }
}
