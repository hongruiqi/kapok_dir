package cn.edu.scut.kapok.distributed.querier.search.fetch;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import com.google.common.io.Closeables;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.zookeeper.server.ByteBufferInputStream;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;

public class SimpleFetcher implements Fetcher {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFetcher.class);

    private final HttpClient httpClient;

    public SimpleFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ListenableFuture<QueryResponse> fetch(final WorkerInfo workerInfo, QueryRequest queryRequest) {
        String uri = generateSearchURI(workerInfo.getAddr());

        final SettableFuture<QueryResponse> future = SettableFuture.create();

        // 建立HTTP请求
        Request req = httpClient.POST(uri);
        req.content(new BytesContentProvider(queryRequest.toByteArray()));

        // 响应回调
        req.onResponseContent(new Response.ContentListener() {
            @Override
            public void onContent(Response response, ByteBuffer content) {
                if (response.getStatus() != HttpStatus.OK_200) {
                    // exception set in onComplete.
                    return;
                }
                InputStream in = new ByteBufferInputStream(content);
                try {
                    QueryResponse searchResponse = QueryResponse.parseFrom(in);
                    future.set(searchResponse);
                } catch (IOException e) {
                    logger.error("parse worker's response", e);
                    future.setException(e);
                } finally {
                    Closeables.closeQuietly(in);
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
                    logger.error("worker response with code: {} {}", workerInfo.getUuid(), result.getResponse().getStatus());
                    future.setException(new FetchException("worker response code error"));
                }
            }
        });
        return future;
    }

    private String generateSearchURI(String workerAddr) {
        HostAndPort addr = HostAndPort.fromString(workerAddr).withDefaultPort(80);
        return URIUtil.newURI("http", addr.getHostText(), addr.getPort(), "/search/", "");
    }
}
