package cn.edu.scut.kapok.distributed.querier.search.resource.merger;


import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.spi.search.WorkerAndQueryResponse;
import cn.edu.scut.kapok.distributed.querier.spi.search.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.spi.search.resource.merger.Merger;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SimpleMerger implements Merger {
    @Override
    public SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException {
        return null;
    }
}
