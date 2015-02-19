import cn.edu.scut.kapok.distributed.common.HttpServer
import cn.edu.scut.kapok.distributed.common.ModuleService
import cn.edu.scut.kapok.distributed.common.node.WorkerManager
import cn.edu.scut.kapok.distributed.common.node.impl.zk.ZooKeeperWorkerManager
import cn.edu.scut.kapok.distributed.querier.QuerierRegistry
import cn.edu.scut.kapok.distributed.querier.api.search.Searcher
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.Fetcher
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.Merger
import cn.edu.scut.kapok.distributed.querier.api.search.resource.selector.Selector
import cn.edu.scut.kapok.distributed.querier.search.impl.KapokSearcher
import cn.edu.scut.kapok.distributed.querier.search.impl.fetch.SimpleFetcher
import cn.edu.scut.kapok.distributed.querier.search.impl.resource.merger.SimpleMerger
import cn.edu.scut.kapok.distributed.querier.search.impl.resource.selector.SimpleSelector
import cn.edu.scut.kapok.distributed.querier.servlet.SearchServlet
import com.google.common.net.HostAndPort
import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.Injector
import com.google.inject.Provider
import com.google.inject.name.Names
import com.google.inject.servlet.GuiceFilter
import com.google.inject.servlet.ServletModule
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.nio.client.HttpAsyncClient
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.servlet.DispatcherType
import java.util.concurrent.Executor

class QuerierModule extends ServletModule implements ModuleService {
    @Override
    protected void configureServlets() {
        bindProperties()
        bindCuratorFramework()
        bindHttpServer()
        bindHttpClient()
        bindComponents()
        bindServlets()
    }

    private void bindProperties() {
        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("kapok.properties")
        if (stream == null) {
            addError("kapok.properties was not found in classpath.")
            return
        }
        try {
            Properties properties = new Properties()
            properties.load(stream)
            Names.bindProperties(binder(), properties)
        } catch (IOException e) {
            addError(e)
        }
    }

    private void bindHttpServer() {
        bind(HttpServer.class).toProvider(new Provider<HttpServer>() {
            @Inject
            @Named("querier.addr")
            String querierAddr

            @Override
            HttpServer get() {
                HostAndPort tcpAddr = HostAndPort.fromString(querierAddr)
                InetSocketAddress sockAddr = new InetSocketAddress(tcpAddr.getHostText(), tcpAddr.getPortOrDefault(8000))
                Server server = new Server(sockAddr)

                ServletContextHandler handler = new ServletContextHandler();
                handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
                server.setHandler(handler);

                return new HttpServer() {
                    @Override
                    void start() throws Exception {
                        server.start()
                    }

                    @Override
                    void join() throws InterruptedException {
                        server.join()
                    }

                    @Override
                    void stop() throws Exception {
                        server.stop()
                    }
                }
            }
        })
    }

    private void bindCuratorFramework() {
        bind(CuratorFramework.class).toProvider(new Provider<CuratorFramework>() {
            @Inject
            @Named("ZooKeeper.connectString")
            String connectString
            @Inject
            @Named("ZooKeeper.sessionTimeout")
            int sessionTimeout

            @Override
            CuratorFramework get() {
                return CuratorFrameworkFactory.builder()
                        .connectString(connectString)
                        .sessionTimeoutMs(sessionTimeout)
                        .retryPolicy(new ExponentialBackoffRetry(1000, 25))
                        .build()
            }
        }).in(Singleton.class)
    }

    private void bindHttpClient() {
        bind(HttpAsyncClient.class).toProvider(new Provider<HttpAsyncClient>() {
            @Override
            HttpAsyncClient get() {
                return HttpAsyncClients.createDefault()
            }
        }).in(Singleton.class)
    }

    private void bindComponents() {
        bind(WorkerManager.class).to(ZooKeeperWorkerManager.class).in(Singleton.class)
        bind(Selector.class).to(SimpleSelector.class).in(Singleton.class)
        bind(Merger.class).to(SimpleMerger.class).in(Singleton.class)
        bind(Fetcher.class).to(SimpleFetcher.class).in(Singleton.class);
        bind(Searcher.class).to(KapokSearcher.class);
        bind(Executor.class).toInstance(MoreExecutors.directExecutor());
    }

    private void bindServlets() {
        serve("/search").with(SearchServlet.class)
    }

    public void start(Injector injector) {
        injector.getInstance(CuratorFramework.class).start()
        injector.getInstance(QuerierRegistry.class).start()
        injector.getInstance(ZooKeeperWorkerManager.class).start();
        ((CloseableHttpAsyncClient) injector.getInstance(HttpAsyncClient.class)).start()
    }

    private void ignoreException(Closure c) {
        try {
            c()
        } catch (Throwable t) { /* ignored. */
        }
    }

    public void stop(Injector injector) {
        ignoreException {
            ((CloseableHttpAsyncClient) injector.getInstance(HttpAsyncClient.class)).close()
        }
        ignoreException {
            injector.getInstance(ZooKeeperWorkerManager.class).close();
        }
        ignoreException {
            injector.getInstance(QuerierRegistry.class).close()
        }
        ignoreException {
            injector.getInstance(CuratorFramework.class).close()
        }
    }
}