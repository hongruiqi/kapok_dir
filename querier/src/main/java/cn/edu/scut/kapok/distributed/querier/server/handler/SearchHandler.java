package cn.edu.scut.kapok.distributed.querier.server.handler;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.search.Searcher;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
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
    private final Searcher searcher;

    public SearchHandler(Searcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void handle(String target, final Request baseRequest, HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        logger.info(request.getMethod());
        if (!request.getMethod().equals("POST")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        SearchRequest searchReq = null;
        try (InputStream in = request.getInputStream()) {
            searchReq = SearchRequest.parseFrom(in);
        } catch (IOException e) {
            logger.error("parse SearchRequest", e);
            throw e;
        }

        Futures.addCallback(searcher.search(searchReq), new FutureCallback<SearchResponse>() {
            @Override
            public void onSuccess(SearchResponse result) {
                try (OutputStream out = response.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.warn("send search response", e);
                }
                baseRequest.setHandled(true);
            }

            @Override
            public void onFailure(Throwable t) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException e) {
                    logger.warn("send search exception", e);
                }
            }
        });
    }
}
