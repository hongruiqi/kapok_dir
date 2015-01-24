package cn.edu.scut.kapok.distributed.worker.server.handler;

import cn.edu.scut.kapok.distributed.worker.fetch.Fetcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SearchHandler extends AbstractHandler {

    private final Fetcher fetcher;

    public SearchHandler(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
    }
}
