package cn.edu.scut.kapok.distributed.worker.fetch;

import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import com.google.common.util.concurrent.ListenableFuture;

public class SampleFetcher implements Fetcher {
    @Override
    public ListenableFuture<QueryResponse> fetch(QueryRequest request) throws FetchException {
        return null;
    }
}
