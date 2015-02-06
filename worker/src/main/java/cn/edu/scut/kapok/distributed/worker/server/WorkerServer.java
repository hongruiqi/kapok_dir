package cn.edu.scut.kapok.distributed.worker.server;

import com.google.common.net.HostAndPort;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import java.net.InetSocketAddress;
import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class WorkerServer {

    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);

    private final Server server;

    @Inject
    public WorkerServer(@Named("worker.Addr") String addr) {
        HostAndPort tcpAddr = HostAndPort.fromString(checkNotNull(addr));
        InetSocketAddress sockAddr = new InetSocketAddress(tcpAddr.getHostText(), tcpAddr.getPortOrDefault(8000));
        server = new Server(sockAddr);
    }

    // Start server.
    public void start() throws Exception {
        // Make requests handled by GuiceFilter.
        ServletContextHandler handler = new ServletContextHandler();
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        server.setHandler(handler);
        // Start server.
        server.start();
        server.join();
    }

    // Stop server.
    public void stop() throws Exception {
        server.stop();
    }
}
