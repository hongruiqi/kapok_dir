package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.querier.search.KapokSearcher;
import cn.edu.scut.kapok.distributed.querier.search.Searcher;
import cn.edu.scut.kapok.distributed.querier.search.fetch.Fetcher;
import cn.edu.scut.kapok.distributed.querier.search.fetch.SimpleFetcher;
import cn.edu.scut.kapok.distributed.querier.search.resource.merger.Merger;
import cn.edu.scut.kapok.distributed.querier.search.resource.merger.SimpleMerger;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.Selector;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.SimpleSelector;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;

import java.util.concurrent.Executor;

// QuerierModule configures inject binders for Querier.
public class QuerierModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Selector.class).to(SimpleSelector.class).asEagerSingleton();
        bind(Merger.class).to(SimpleMerger.class).asEagerSingleton();
        bind(Fetcher.class).to(SimpleFetcher.class).asEagerSingleton();
        bind(Searcher.class).to(KapokSearcher.class);
        bind(Executor.class).toInstance(MoreExecutors.directExecutor());
    }
}
