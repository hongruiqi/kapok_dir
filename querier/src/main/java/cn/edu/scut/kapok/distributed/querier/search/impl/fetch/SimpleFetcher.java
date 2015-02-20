package cn.edu.scut.kapok.distributed.querier.search.impl.fetch;

import cn.edu.scut.kapok.distributed.common.httpclient.ProtoBufferHttpClient;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.FetchException;
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.Fetcher;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;

@Singleton
public class SimpleFetcher implements Fetcher {

    private final ProtoBufferHttpClient client;

    @Inject
    public SimpleFetcher(HttpAsyncClient httpClient) {
        this.client = new ProtoBufferHttpClient(httpClient);
    }

    @Override
    public ListenableFuture<QueryResponse> fetch(final WorkerInfo workerInfo, QueryRequest queryRequest) throws FetchException {
        return client.execute(workerInfo.getAddr(), queryRequest, QueryResponse.PARSER);
    }
}

