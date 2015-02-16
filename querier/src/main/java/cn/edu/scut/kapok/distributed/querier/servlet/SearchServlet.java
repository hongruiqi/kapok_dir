package cn.edu.scut.kapok.distributed.querier.servlet;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.search.spi.SearchException;
import cn.edu.scut.kapok.distributed.querier.search.spi.Searcher;
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

@Singleton
public class SearchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);

    private final Searcher searcher;

    @Inject
    public SearchServlet(Searcher searcher) {
        this.searcher = searcher;
    }

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext asyncContext = req.getAsyncContext();

        SearchRequest searchReq;
        try (InputStream in = req.getInputStream()) {
            searchReq = SearchRequest.parseFrom(in);
        } catch (IOException e) {
            logger.error("parse SearchRequest", e);
            throw e;
        }

        ListenableFuture<SearchResponse> future;
        try {
            future = searcher.search(searchReq);
        } catch (SearchException e) {
            logger.error("search", e);
            throw new ServletException(e);
        }
        checkNotNull(future);
        Futures.addCallback(future, new FutureCallback<SearchResponse>() {
            @Override
            public void onSuccess(SearchResponse result) {
                try (OutputStream out = resp.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.error("serialize and write SearchResponse", e);
                    // ignored intended.
                } finally {
                    asyncContext.complete();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("search error", t);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                asyncContext.complete();
            }
        });
    }
}
