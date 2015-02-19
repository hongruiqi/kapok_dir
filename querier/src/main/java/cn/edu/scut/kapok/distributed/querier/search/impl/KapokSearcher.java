package cn.edu.scut.kapok.distributed.querier.search.impl;

import cn.edu.scut.kapok.distributed.common.node.WorkerManager;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.search.spi.SearchException;
import cn.edu.scut.kapok.distributed.querier.search.spi.Searcher;
import cn.edu.scut.kapok.distributed.querier.search.spi.WorkerAndQueryResponse;
import cn.edu.scut.kapok.distributed.querier.search.spi.fetch.Fetcher;
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.merger.Merger;
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.selector.SelectException;
import cn.edu.scut.kapok.distributed.querier.search.spi.resource.selector.Selector;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Singleton
public class KapokSearcher implements Searcher {

    private final WorkerManager workerManager;
    private final Fetcher fetcher;
    private final Selector selector;
    private final Merger merger;
    private final Executor executor;

    @Inject
    public KapokSearcher(WorkerManager workerManager, Fetcher fetcher,
                         Selector selector, Merger merger,
                         Executor executor) {
        this.workerManager = workerManager;
        this.fetcher = fetcher;
        this.selector = selector;
        this.merger = merger;
        this.executor = executor;
    }

    public ListenableFuture<SearchResponse> search(SearchRequest request) {
        final SettableFuture<SearchResponse> future = SettableFuture.create();

        List<WorkerInfo> candidateWorkers;
        if (request.getResourcesCount() == 0) {
            candidateWorkers = workerManager.getWorkers().values().asList();
        } else {
            candidateWorkers = resourcesToWorkerInfo(request.getResourcesList());
        }

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
}
