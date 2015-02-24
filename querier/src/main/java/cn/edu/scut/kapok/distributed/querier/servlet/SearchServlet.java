package cn.edu.scut.kapok.distributed.querier.servlet;

import cn.edu.scut.kapok.distributed.common.http.ProtoBufferHttpServlet;
import cn.edu.scut.kapok.distributed.protos.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.Searcher;
import com.google.common.util.concurrent.ListenableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * SearchServlet handles search request.
 * It parses input message, calls {@code searcher.search()},
 * and finally generate output messge from search result.
 */
@Singleton
public class SearchServlet extends ProtoBufferHttpServlet<SearchRequest, SearchResponse> {

    private final Searcher searcher;

    /**
     * Create SearchServlet instance.
     *
     * @param searcher Searcher is used to process search request.
     */
    @Inject
    public SearchServlet(Searcher searcher) {
        super(SearchRequest.PARSER);
        this.searcher = searcher;
    }

    @Override
    protected ListenableFuture<SearchResponse> process(SearchRequest request) {
        return searcher.search(request);
    }
}
