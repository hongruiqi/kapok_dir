package cn.edu.scut.kapok.distributed.querier.server.handler;

import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.search.Searcher;
import com.google.common.base.Preconditions;
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
    private Searcher searcher;

    public SearchHandler(Searcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

        SearchResponse searchResp;
        try {
            searchResp = searcher.search(searchReq).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
        Preconditions.checkNotNull(searchResp);
        try (OutputStream out = response.getOutputStream()) {
            searchResp.writeTo(out);
        } catch (IOException e) {
            logger.error("generate SearchResponse", e);
            throw e;
        }
        baseRequest.setHandled(true);
    }
}
