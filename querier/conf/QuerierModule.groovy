import cn.edu.scut.kapok.distributed.common.ModuleService
import cn.edu.scut.kapok.distributed.querier.QuerierRegistry
import cn.edu.scut.kapok.distributed.querier.search.impl.KapokSearcher
import cn.edu.scut.kapok.distributed.querier.search.impl.fetch.SimpleFetcher
import cn.edu.scut.kapok.distributed.querier.search.impl.resource.merger.SimpleMerger
import cn.edu.scut.kapok.distributed.querier.search.impl.resource.selector.SimpleSelector
import cn.edu.scut.kapok.distributed.querier.search.spi.Searcher
import cn.edu.scut.kapok.distributed.querier.search.spi.fetch.Fetcher
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.merger.Merger
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.selector.Selector
import cn.edu.scut.kapok.distributed.querier.servlet.SearchServlet
import cn.edu.scut.kapok.distributed.querier.servlet.TestServlet
import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.Injector
import com.google.inject.Provider
import com.google.inject.name.Names
import com.google.inject.servlet.ServletModule
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.util.concurrent.Executor

class QuerierModule extends ServletModule implements ModuleService {
    @Override
    protected void configureServlets() {
        bindProperties()
        bindCuratorFramework()

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

    private void bindComponents() {
        bind(Selector.class).to(SimpleSelector.class).asEagerSingleton();
        bind(Merger.class).to(SimpleMerger.class).asEagerSingleton();
        bind(Fetcher.class).to(SimpleFetcher.class).asEagerSingleton();
        bind(Searcher.class).to(KapokSearcher.class);
        bind(Executor.class).toInstance(MoreExecutors.directExecutor());
    }

    private void bindServlets() {
        serve("/test").with(TestServlet.class);
        serve("/search").with(SearchServlet.class);
    }

    public void start(Injector injector) {
        injector.getInstance(CuratorFramework.class).start()
        injector.getInstance(QuerierRegistry.class).start()
    }

    public void stop(Injector injector) {
        injector.getInstance(QuerierRegistry.class).close()
        injector.getInstance(CuratorFramework.class).close()
    }
}