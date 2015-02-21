package cn.edu.scut.kapok.distributed.worker.servlet;

import cn.edu.scut.kapok.distributed.protos.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.worker.api.retriever.RetrieveException;
import cn.edu.scut.kapok.distributed.worker.api.retriever.Retriever;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SearchServlet deals with search request.
 */
@Singleton
public class SearchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);

    private final Retriever retriever;

    /**
     * Create SearchServlet instance.
     *
     * @param retriever Retriever is used to retrieve results with provided query.
     */
    @Inject
    public SearchServlet(Retriever retriever) {
        this.retriever = retriever;
    }

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // Use async.
        final AsyncContext asyncContext = req.startAsync();

        // Parse input.
        QueryRequest queryRequest;
        try (InputStream in = req.getInputStream()) {
            queryRequest = QueryRequest.parseFrom(in);
        } catch (IOException e) {
            logger.debug("parse request error.", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Retrieve.
        ListenableFuture<QueryResponse> future;
        try {
            future = retriever.retrieve(queryRequest);
        } catch (RetrieveException e) {
            throw new ServletException(e);
        }
        checkNotNull(future);

        // Add callback to be called with result and generate response.
        addCallback(future, new FutureCallback<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse result) {
                try (OutputStream out = resp.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.debug("write response error.", e);
                } finally {
                    asyncContext.complete();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException e) {
                    logger.debug("write response error.", e);
                } finally {
                    asyncContext.complete();
                }
            }
        });
    }

    void addCallback(ListenableFuture<QueryResponse> future, FutureCallback<QueryResponse> callback) {
        Futures.addCallback(future, callback);
    }
}
