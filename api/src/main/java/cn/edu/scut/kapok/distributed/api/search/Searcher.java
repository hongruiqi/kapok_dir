package cn.edu.scut.kapok.distributed.api.search;

import cn.edu.scut.kapok.distributed.api.search.exception.QuerierNotFoundException;
import cn.edu.scut.kapok.distributed.api.search.exception.SearchException;
import cn.edu.scut.kapok.distributed.api.search.querier.provider.QuerierProvider;
import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import cn.edu.scut.kapok.distributed.protos.QueryProto.Query;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchProto.SearchResponse;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class Searcher {

    private static final Logger logger = LoggerFactory.getLogger(Searcher.class);

    private QuerierProvider querierProvider;
    private HttpClient httpClient;

    @Inject
    public Searcher(HttpClient httpClient, QuerierProvider querierProvider) {
        this.httpClient = checkNotNull(httpClient);
        this.querierProvider = checkNotNull(querierProvider);
    }

    public ListenableFuture<SearchResponse> search(Query query, int page, int perPage, List<String> resources)
            throws QuerierNotFoundException {
        checkArgument(page > 0, "page should be positive");
        checkArgument(perPage > 0, "perPage should be positive");
        checkNotNull(query, "query can't be null");
        checkNotNull(resources, "resources can't be null");

        // 构建查询ProtoBuf对象
        SearchRequest search = SearchRequest.newBuilder()
                .setPage(page).setPerPage(perPage).setQuery(query)
                .addAllResources(resources).build();
        byte[] searchBytes = search.toByteArray();

        // 响应Future对象
        final SettableFuture<SearchResponse> future = SettableFuture.create();

        // 获取Querier服务器地址
        QuerierInfo querierInfo = querierProvider.get();
        logger.debug("use querier server: {}", querierInfo);

        HostAndPort addr = HostAndPort.fromString(querierInfo.getAddr()).withDefaultPort(80);
        String uri = URIUtil.newURI("http", addr.getHostText(), addr.getPort(), "/search/", "");

        // 建立HTTP请求
        Request req = httpClient.POST(uri);
        req.content(new BytesContentProvider(searchBytes));

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
                    SearchResponse searchResponse = SearchResponse.parseFrom(in);
                    future.set(searchResponse);
                } catch (IOException e) {
                    logger.error("parse search response", e);
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
                    logger.error("querier response with code: {}", result.getResponse().getStatus());
                    future.setException(new SearchException("querier response code error"));
                }
            }
        });

        return future;
    }
}
