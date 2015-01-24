package cn.edu.scut.kapok.distributed.querier.search;

import cn.edu.scut.kapok.distributed.common.node.WorkerManager;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.search.fetch.Fetcher;
import cn.edu.scut.kapok.distributed.querier.search.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.search.resource.merger.Merger;
import cn.edu.scut.kapok.distributed.querier.search.resource.merger.SimpleMerger;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.SelectException;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.Selector;
import cn.edu.scut.kapok.distributed.querier.search.resource.selector.SimpleSelector;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.*;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.*;

public class KapokSearcher implements Searcher {

    private static final Logger logger = LoggerFactory.getLogger(KapokSearcher.class);

    private final CuratorFramework cf;
    private final Fetcher fetcher;
    private final Selector selector;
    private final Merger merger;
    private final Executor executor;

    private WorkerManager workerManager;
    private AtomicReference<State> state = new AtomicReference<>(State.LATENT);

    private KapokSearcher(CuratorFramework cf, Fetcher fetcher,
                          Selector selector, Merger merger,
                          Executor executor) {
        this.cf = cf;
        this.selector = selector;
        this.merger = merger;
        this.executor = executor;
        this.fetcher = fetcher;
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

    public ListenableFuture<SearchResponse> search(SearchRequest request) {
        checkState(!isActive(), "KapokSearcher must be started before calling this method");

        final SettableFuture<SearchResponse> future = SettableFuture.create();

        List<WorkerInfo> candidateWorkers = resourcesToWorkerInfo(request.getResourcesList());
        final List<WorkerInfo> workers;
        try {
            workers = selector.selectResource(request.getQuery(), candidateWorkers);
        } catch (SelectException e) {
            future.setException(new SearchException(e));
            return future;
        }

        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setQuery(request.getQuery())
                .setFrom(0)
                .setCount(request.getPage() * request.getPerPage())
                .build();

        List<ListenableFuture<QueryResponse>> futures = new ArrayList<>(workers.size());
        for (WorkerInfo worker : workers) {
            futures.add(fetcher.fetch(worker, queryRequest));
        }

        Futures.addCallback(Futures.successfulAsList(futures), new FutureCallback<List<QueryResponse>>() {
            @Override
            public void onSuccess(List<QueryResponse> result) {
                List<WorkerAndQueryResponse> results = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    results.add(new WorkerAndQueryResponse(workers.get(i), Optional.of(result.get(i))));
                }
                try {
                    SearchResponse searchResponse = merger.merge(results);
                    future.set(searchResponse);
                } catch (MergeException t) {
                    future.setException(t);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, executor);

        return future;
    }

    private List<WorkerInfo> resourcesToWorkerInfo(List<String> resources) {
        Map<String, WorkerInfo> allWorkers = workerManager.getWorkers();
        List<WorkerInfo> workers = new ArrayList<>();
        for (String resource : resources) {
            if (allWorkers.containsKey(resource)) {
                workers.add(allWorkers.get(resource));
            }
        }
        return workers;
    }

    private enum State {LATENT, STARTED, CLOSED}

    public static class Builder {
        private CuratorFramework cf;
        private Selector selector;
        private Merger merger;
        private Executor executor;
        private Fetcher fetcher;

        public Builder setCuratorFramework(CuratorFramework cf) {
            this.cf = checkNotNull(cf);
            return this;
        }

        public Builder setSelector(Selector selector) {
            this.selector = checkNotNull(selector);
            return this;
        }

        public Builder setMerger(Merger merger) {
            this.merger = checkNotNull(merger);
            return this;
        }

        public Builder setExecutor(ExecutorService executor) {
            this.executor = checkNotNull(executor);
            return this;
        }

        public Builder setFetcher(Fetcher fetcher) {
            this.fetcher = checkNotNull(fetcher);
            return this;
        }

        public KapokSearcher build() {
            checkNotNull(cf, "CuratorFramework should be set");
            checkNotNull(fetcher);
            if (selector == null) {
                selector = new SimpleSelector();
            }
            if (merger == null) {
                merger = new SimpleMerger();
            }
            if (executor == null) {
                executor = MoreExecutors.directExecutor();
            }
            return new KapokSearcher(cf, fetcher, selector, merger, executor);
        }
    }
}
