package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.querier.search.KapokSearcher;
import cn.edu.scut.kapok.distributed.querier.search.Searcher;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.DefaultResourceSelector;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.ResourceSelector;
import com.google.inject.AbstractModule;

// QuerierModule configures inject binders for Querier.
public class QuerierModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceSelector.class).to(DefaultResourceSelector.class);
        bind(Searcher.class).to(KapokSearcher.class);
    }
}
