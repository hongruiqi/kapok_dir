package cn.edu.scut.kapok.distributed.querier.server;

import cn.edu.scut.kapok.distributed.querier.search.KapokSearcher;
import cn.edu.scut.kapok.distributed.querier.server.handler.SearchHandler;
import org.apache.curator.framework.CuratorFramework;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.*;

// QuerierServer processes the request from querier's client.
public class QuerierServer {

    private static final Logger logger = LoggerFactory.getLogger(QuerierServer.class);

    private final String host;
    private final int port;
    private final Server server;
    private final CuratorFramework cf;

    public QuerierServer(String host, int port, CuratorFramework cf) {
        this.host = checkNotNull(host);
        checkArgument(port >= 0, "port can't be negative: %s", port);
        this.port = port;
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        server = new Server(socketAddress);
        this.cf = checkNotNull(cf);
    }

    private void registerHandlers(Server server) throws Exception {
        ContextHandler searchContext = new ContextHandler("/search");
        // Search Handler
        KapokSearcher searcher = new KapokSearcher.Builder()
                .setCuratorFramework(cf)
                .build();
        searcher.start();
        searchContext.setHandler(new SearchHandler(searcher));

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new ContextHandler[]{
                searchContext
        });
        server.setHandler(contexts);
    }

    public void start() throws Exception {
        registerHandlers(server);
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
