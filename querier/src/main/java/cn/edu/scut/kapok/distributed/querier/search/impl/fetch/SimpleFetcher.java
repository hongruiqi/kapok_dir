package cn.edu.scut.kapok.distributed.querier.search.impl.fetch;

import cn.edu.scut.kapok.distributed.common.httpclient.ProtoBufferHttpClient;
import cn.edu.scut.kapok.distributed.protos.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.Fetcher;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SimpleFetcher implements Fetcher {

    private final ProtoBufferHttpClient client;

    @Inject
    public SimpleFetcher(HttpAsyncClient httpClient) {
        this.client = new ProtoBufferHttpClient(httpClient);
    }

    @Override
    public ListenableFuture<QueryResponse> fetch(final WorkerInfo workerInfo, QueryRequest queryRequest) {
        return client.execute(workerInfo.getAddr(), queryRequest, QueryResponse.PARSER);
    }
}

