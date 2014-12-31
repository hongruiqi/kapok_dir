package cn.edu.scut.kapok.distributed.querier.search;

import cn.edu.scut.kapok.distributed.common.node.WorkerManager;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.DefaultResourceSelector;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.ResourceSelector;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.*;

public class KapokSearcher implements Searcher {

    private static final Logger logger = LoggerFactory.getLogger(KapokSearcher.class);
    private AtomicReference<State> state = new AtomicReference<>(State.LATENT);
    private CuratorFramework cf;
    private WorkerManager workerManager;
    private ResourceSelector selector;
    private KapokSearcher() {

    }

    public void start() throws Exception {
        checkState(state.compareAndSet(State.LATENT, State.STARTED), "Already started");
        workerManager = new WorkerManager(cf);
    }

    public void close() throws IOException {
        if (!state.compareAndSet(State.STARTED, State.CLOSED)) {
            return;
        }
        workerManager.close();
    }

    public boolean isActive() {
        return state.get() == State.STARTED;
    }

    public SearchResponse search(SearchRequest request) throws Exception {
        checkState(!isActive(), "KapokSearcher must be started before calling this method");

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

    private enum State {LATENT, STARTED, CLOSED}

    public static class Builder {
        private CuratorFramework cf;
        private ResourceSelector selector;

        public Builder setCuratorFramework(CuratorFramework cf) {
            this.cf = checkNotNull(cf);
            return this;
        }

        public Builder setResourceSelector(ResourceSelector selector) {
            this.selector = checkNotNull(selector);
            return this;
        }

        public KapokSearcher build() {
            checkNotNull(cf, "CuratorFramework should be set");
            if (selector == null) {
                selector = new DefaultResourceSelector();
            }
            KapokSearcher searcher = new KapokSearcher();
            searcher.cf = cf;
            searcher.selector = selector;
            return searcher;
        }
    }
}
