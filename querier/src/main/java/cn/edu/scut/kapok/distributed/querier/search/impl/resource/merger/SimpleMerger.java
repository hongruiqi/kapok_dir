package cn.edu.scut.kapok.distributed.querier.search.impl.resource.merger;

import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.WorkerAndQueryResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.Merger;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SimpleMerger implements Merger {
    @Override
    public SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException {
        return null;
    }
}
