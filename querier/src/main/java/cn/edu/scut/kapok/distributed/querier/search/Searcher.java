package cn.edu.scut.kapok.distributed.querier.search;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;

public interface Searcher {
    SearchResponse search(SearchRequest request) throws Exception;
}
