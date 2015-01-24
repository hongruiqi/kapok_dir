package cn.edu.scut.kapok.distributed.worker.server.handler;

import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.worker.fetch.Fetcher;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SearchHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(SearchHandler.class);

    private final Fetcher fetcher;

    public SearchHandler(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public void handle(String target, final Request baseRequest, HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        QueryRequest queryRequest;
        try (InputStream in = baseRequest.getInputStream()) {
            queryRequest = QueryRequest.parseFrom(in);
        } catch (IOException e) {
            logger.warn("reading input", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ListenableFuture<QueryResponse> future = fetcher.fetch(queryRequest);
        Futures.addCallback(future, new FutureCallback<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse result) {
                try (OutputStream out = response.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.error("sending worker response", e);
                }
                baseRequest.setHandled(true);
            }

            @Override
            public void onFailure(Throwable t) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException e) {
                    logger.warn("sending worker error", e);
                }
                baseRequest.setHandled(true);
            }
        });
    }
}
