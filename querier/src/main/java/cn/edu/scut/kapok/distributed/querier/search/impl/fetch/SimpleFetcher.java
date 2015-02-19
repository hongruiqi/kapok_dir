package cn.edu.scut.kapok.distributed.querier.search.impl.fetch;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.FetchException;
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.Fetcher;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.CodedInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;

@Singleton
public class SimpleFetcher implements Fetcher {

    private final HttpAsyncClient httpClient;

    @Inject
    public SimpleFetcher(HttpAsyncClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ListenableFuture<QueryResponse> fetch(final WorkerInfo workerInfo, QueryRequest queryRequest) throws FetchException {
        String uri = workerInfo.getAddr();

        final SettableFuture<QueryResponse> future = createFuture();

        // Make HTTP Request.
        HttpPost request = new HttpPost(uri);
        request.setEntity(new ByteArrayEntity(queryRequest.toByteArray()));

        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    future.setException(new FetchException("response code error"));
                    return;
                }
                try {
                    QueryResponse searchResponse = QueryResponse.parseFrom(
                            CodedInputStream.newInstance(result.getEntity().getContent()));
                    future.set(searchResponse);
                } catch (IOException e) {
                    future.setException(new FetchException(e));
                }
            }

            @Override
            public void failed(Exception ex) {
                future.setException(new FetchException(ex));
            }

            @Override
            public void cancelled() {
                future.setException(new FetchException("fetch cancelled"));
            }
        });

        return future;
    }

    SettableFuture<QueryResponse> createFuture() {
        return SettableFuture.create();
    }
}

