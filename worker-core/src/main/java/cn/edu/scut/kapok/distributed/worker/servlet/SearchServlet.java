package cn.edu.scut.kapok.distributed.worker.servlet;

import cn.edu.scut.kapok.distributed.common.http.ProtoBufferHttpServlet;
import cn.edu.scut.kapok.distributed.protos.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.worker.api.retriever.Retriever;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * SearchServlet deals with search request.
 */
@Singleton
public class SearchServlet extends ProtoBufferHttpServlet<QueryRequest, QueryResponse> {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);

    private final Retriever retriever;

    /**
     * Create SearchServlet instance.
     *
     * @param retriever Retriever is used to retrieve results with provided query.
     */
    @Inject
    public SearchServlet(Retriever retriever) {
        super(QueryRequest.PARSER);
        this.retriever = retriever;
    }

    @Override
    protected ListenableFuture<QueryResponse> process(QueryRequest request) {
        return retriever.retrieve(request);
    }
}
