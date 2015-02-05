package cn.edu.scut.kapok.distributed.querier.search;

import cn.edu.scut.kapok.distributed.common.node.WorkerManager;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.ResourceSelector;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;

@Singleton
public class KapokSearcher implements Searcher {

    private static final Logger logger = LoggerFactory.getLogger(KapokSearcher.class);

    private final WorkerManager workerManager;
    private final ResourceSelector selector;

    @Inject
    public KapokSearcher(WorkerManager workerManager, ResourceSelector selector) {
        this.workerManager = workerManager;
        this.selector = selector;
    }

    public ListenableFuture<SearchResponse> search(SearchRequest request) throws Exception {
        // Filter out resources that is not working.
        final Map<String, WorkerInfo> workers = workerManager.getWorkers();
        Collection<String> resources = Collections2.filter(request.getResourcesList(),
                new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return workers.containsKey(input);
                    }
                });
        resources = selector.selectResource(request.getQuery(), resources);
        return null;
    }
}
