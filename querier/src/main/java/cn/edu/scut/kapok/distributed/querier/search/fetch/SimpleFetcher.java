package cn.edu.scut.kapok.distributed.querier.search.fetch;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.spi.search.fetch.FetchException;
import cn.edu.scut.kapok.distributed.querier.spi.search.fetch.Fetcher;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.CodedInputStream;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.URIUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.ByteBuffer;

import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;

@Singleton
public class SimpleFetcher implements Fetcher {

    private final HttpClient httpClient;

    @Inject
    public SimpleFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ListenableFuture<QueryResponse> fetch(final WorkerInfo workerInfo, QueryRequest queryRequest) {
        String uri = generateSearchURI(workerInfo.getAddr());

        final SettableFuture<QueryResponse> future = SettableFuture.create();

        // Make HTTP Request.
        Request req = httpClient.POST(uri);
        req.content(new BytesContentProvider(queryRequest.toByteArray()));

        // Response callback.
        req.onResponseContent(new Response.ContentListener() {
            @Override
            public void onContent(Response response, ByteBuffer content) {
                if (response.getStatus() != HttpStatus.OK_200) {
                    // exception set in onComplete.
                    return;
                }
                try {
                    QueryResponse searchResponse = QueryResponse.parseFrom(
                            CodedInputStream.newInstance(content));
                    future.set(searchResponse);
                } catch (IOException e) {
                    future.setException(e);
                }
            }
        }).send(new Response.CompleteListener() {
            @Override
            public void onComplete(Result result) {
                if (!result.isSucceeded()) {
                    future.setException(result.getFailure());
                    return;
                }
                if (result.getResponse().getStatus() != HttpStatus.OK_200) {
                    future.setException(new FetchException("worker response code error"));
                }
            }
        });
        return future;
    }

    private String generateSearchURI(String workerAddr) {
        HostAndPort addr = HostAndPort.fromString(workerAddr).withDefaultPort(8000);
        return URIUtil.newURI("http", addr.getHostText(), addr.getPort(), "/search", "");
    }
}

