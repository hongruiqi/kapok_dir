package cn.edu.scut.kapok.distributed.querier.search.impl.resource.merger;

import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.WorkerAndQueryResponse;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.MergeException;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.merger.Merger;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SimpleMerger implements Merger {
    @Override
    public SearchResponse merge(List<WorkerAndQueryResponse> results) throws MergeException {
        SearchResponse.Builder builder = SearchResponse.newBuilder();
        long totalHit = 0;
        for (WorkerAndQueryResponse r : results) {
            if (r.getQueryResponse().isPresent()) {
                QueryResponse resp = r.getQueryResponse().get();
                long total = resp.getTotal();
                builder.addResourceStatsBuilder()
                        .setResource(r.getWorker().getUuid())
                        .setHit(total);
                totalHit += total;
            }
        }
        builder.setTotalHit(totalHit);
        return builder.build();
    }
}
