package cn.edu.scut.kapok.distributed.querier.spi.search;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import com.google.common.util.concurrent.ListenableFuture;

public interface Searcher {
    ListenableFuture<SearchResponse> search(SearchRequest request) throws SearchException;
}
