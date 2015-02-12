package cn.edu.scut.kapok.distributed.querier.spi.search.resource.merger;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.spi.search.WorkerAndQueryResponse;

import java.util.List;

public interface Merger {
    SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException;
}
