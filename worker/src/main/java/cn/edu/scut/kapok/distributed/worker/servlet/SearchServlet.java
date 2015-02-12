package cn.edu.scut.kapok.distributed.worker.servlet;

import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.worker.spi.Retriever;
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

@Singleton
public class SearchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);

    private final Retriever retriever;

    @Inject
    public SearchServlet(Retriever retriever) {
        this.retriever = retriever;
    }

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext asyncContext = req.startAsync();

        QueryRequest queryRequest;
        try (InputStream in = req.getInputStream()) {
            queryRequest = QueryRequest.parseFrom(in);
        } catch (IOException e) {
            logger.warn("reading input", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ListenableFuture<QueryResponse> future = retriever.retrieve(queryRequest);
        Futures.addCallback(future, new FutureCallback<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse result) {
                try (OutputStream out = resp.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.error("sending worker response", e);
                } finally {
                    asyncContext.complete();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException e) {
                    logger.warn("sending worker error", e);
                } finally {
                    asyncContext.complete();
                }
            }
        });
    }
}
