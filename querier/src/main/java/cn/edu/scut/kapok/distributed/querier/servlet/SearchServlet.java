package cn.edu.scut.kapok.distributed.querier.servlet;

import cn.edu.scut.kapok.distributed.protos.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.Searcher;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
 * SearchServlet handles search request.
 * It parses input message, calls {@code searcher.search()},
 * and finally generate output messge from search result.
 */
@Singleton
public class SearchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);

    private final Searcher searcher;

    /**
     * Create SearchServlet instance.
     *
     * @param searcher Searcher is used to process search request.
     */
    @Inject
    public SearchServlet(Searcher searcher) {
        this.searcher = searcher;
    }

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext asyncContext = req.startAsync();

        // Parse request.
        SearchRequest searchReq;
        try (InputStream in = req.getInputStream()) {
            searchReq = SearchRequest.parseFrom(in);
        } catch (IOException e) {
            logger.debug("parse SearchRequest", e);
            throw e;
        }

        // Process search request.
        ListenableFuture<SearchResponse> future = searcher.search(searchReq);
        checkNotNull(future);

        // Add callback to be called with result.
        Futures.addCallback(future, new FutureCallback<SearchResponse>() {
            @Override
            public void onSuccess(SearchResponse result) {
                try (OutputStream out = resp.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.debug("serialize and write SearchResponse", e);
                } finally {
                    asyncContext.complete();
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable t) {
                logger.error("search error", t);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                asyncContext.complete();
            }
        });
    }
}
