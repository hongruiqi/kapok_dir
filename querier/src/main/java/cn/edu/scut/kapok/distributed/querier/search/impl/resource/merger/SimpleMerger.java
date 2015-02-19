package cn.edu.scut.kapok.distributed.querier.search.impl.resource.merger;


import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.search.spi.WorkerAndQueryResponse;
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.merger.Merger;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SimpleMerger implements Merger {
    @Override
    public SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException {
        return null;
    }
}