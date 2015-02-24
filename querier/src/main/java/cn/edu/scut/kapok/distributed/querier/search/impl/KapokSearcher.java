package cn.edu.scut.kapok.distributed.querier.search.impl;

import cn.edu.scut.kapok.distributed.common.node.WorkerMonitor;
import cn.edu.scut.kapok.distributed.protos.*;
import cn.edu.scut.kapok.distributed.querier.api.search.SearchException;
import cn.edu.scut.kapok.distributed.querier.api.search.Searcher;
import cn.edu.scut.kapok.distributed.querier.api.search.WorkerAndQueryResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.Fetcher;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.Merger;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.selector.SelectException;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.selector.Selector;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Singleton
public class KapokSearcher implements Searcher {

    private final WorkerMonitor workerMonitor;
    private final Fetcher fetcher;
    private final Selector selector;
    private final Merger merger;
    private final Executor executor;

    /**
     * Create KapokSearch instance.
     *
     * @param workerMonitor Used to get workers' information.
     * @param fetcher       Used to communicate with worker.
     * @param selector      Used to resource select.
     * @param merger        Used to result merge.
     * @param executor      Used to execute callback.
     */
    @Inject
    public KapokSearcher(WorkerMonitor workerMonitor, Fetcher fetcher,
                         Selector selector, Merger merger,
                         Executor executor) {
        this.workerMonitor = workerMonitor;
        this.fetcher = fetcher;
        this.selector = selector;
        this.merger = merger;
        this.executor = executor;
    }

    /**
     * Do searching.
     *
     * @param request Request to be searched.
     * @return Future of the SearchResponse.
     */
    public ListenableFuture<SearchResponse> search(SearchRequest request) {
        final SettableFuture<SearchResponse> future = SettableFuture.create();

        // Generate candidate workers.
        List<WorkerInfo> candidateWorkers;
        if (request.getResourcesCount() == 0) {
            // If the resource list requested is empty, means all resources is used.
            candidateWorkers = workerMonitor.getWorkers().values().asList();
        } else {
            // Translate resource list to WorkerInfo list.
            candidateWorkers = resourcesToWorkerInfo(request.getResourcesList());
        }

        // Resource select.
        final List<WorkerInfo> workers;
        try {
            workers = selector.selectResource(request.getQuery(), candidateWorkers);
        } catch (SelectException e) {
            future.setException(new SearchException(e));
            return future;
        }

        // Build query request.
        // Query request is generated from the search request.
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setQuery(request.getQuery())
                .setFrom(0)
                .setCount(request.getPage() * request.getPerPage())
                .build();

        // Loop for all selected worker to do search and collect workers' futures.
        List<ListenableFuture<QueryResponse>> futures = new ArrayList<>(workers.size());
        for (WorkerInfo worker : workers) {
            try {
                futures.add(fetcher.fetch(worker, queryRequest));
            } catch (Throwable t) {
                // Catch unchecked exception, and generate a fake future for the worker.
                // Failure of one worker should not affect the others.
                SettableFuture<QueryResponse> f = SettableFuture.create();
                f.setException(t);
                futures.add(f);
            }
        }

        // Add callback to be called when all futures of the workers are resolved,
        // whether successed of failed. The callback merges results and sets SearchResponse's future.
        Futures.addCallback(Futures.successfulAsList(futures), new FutureCallback<List<QueryResponse>>() {
            @Override
            public void onSuccess(List<QueryResponse> result) {
                List<WorkerAndQueryResponse> results = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    if (result.get(i)!=null) {
                        results.add(new WorkerAndQueryResponse(workers.get(i), result.get(i)));
                    }
                }
                // Merge.
                try {
                    SearchResponse searchResponse = merger.merge(results);
                    future.set(searchResponse);
                } catch (MergeException t) {
                    future.setException(t);
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable t) {
            }
        }, executor);

        return future;
    }

    // Translate resource list of workers' uuid to WorkerInfo list.
    private List<WorkerInfo> resourcesToWorkerInfo(List<String> resources) {
        Map<String, WorkerInfo> allWorkers = workerMonitor.getWorkers();
        List<WorkerInfo> workers = new ArrayList<>();
        for (String resource : resources) {
            if (allWorkers.containsKey(resource)) {
                workers.add(allWorkers.get(resource));
            }
        }
        return workers;
    }
}
