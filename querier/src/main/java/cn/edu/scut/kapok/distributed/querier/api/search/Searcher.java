package cn.edu.scut.kapok.distributed.querier.api.search;

import cn.edu.scut.kapok.distributed.protos.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Searchers implement this interface to support searching.
 */
public interface Searcher {
    ListenableFuture<SearchResponse> search(SearchRequest request);
}
