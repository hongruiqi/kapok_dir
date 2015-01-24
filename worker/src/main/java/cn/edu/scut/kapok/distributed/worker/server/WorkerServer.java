package cn.edu.scut.kapok.distributed.worker.server;

import cn.edu.scut.kapok.distributed.worker.fetch.Fetcher;
import cn.edu.scut.kapok.distributed.worker.fetch.SampleFetcher;
import cn.edu.scut.kapok.distributed.worker.server.handler.SearchHandler;
import org.apache.curator.framework.CuratorFramework;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.*;

public class WorkerServer {

    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);

    private final String host;
    private final int port;
    private final Server server;
    private final CuratorFramework cf;

    public WorkerServer(String host, int port, CuratorFramework cf) {
        this.host = checkNotNull(host);
        checkArgument(port >= 0, "port can't be negative: %s", port);
        this.port = port;
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        server = new Server(socketAddress);
        this.cf = checkNotNull(cf);
    }

    private void registerHandlers(Server server) throws Exception {
        ContextHandler searchContext = new ContextHandler("/search");
        Fetcher fetcher = new SampleFetcher();
        searchContext.setHandler(new SearchHandler(fetcher));

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
