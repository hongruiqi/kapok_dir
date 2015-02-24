package cn.edu.scut.kapok.distributed.api.search;

import cn.edu.scut.kapok.distributed.api.search.querier.provider.QuerierNotFoundException;
import cn.edu.scut.kapok.distributed.api.search.querier.provider.QuerierProvider;
import cn.edu.scut.kapok.distributed.common.http.ProtoBufferHttpClient;
import cn.edu.scut.kapok.distributed.protos.QuerierInfo;
import cn.edu.scut.kapok.distributed.protos.Query;
import cn.edu.scut.kapok.distributed.protos.SearchRequest;
import cn.edu.scut.kapok.distributed.protos.SearchResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.nio.client.HttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class Searcher {

    private static final Logger logger = LoggerFactory.getLogger(Searcher.class);

    private QuerierProvider querierProvider;
    private ProtoBufferHttpClient client;

    @Inject
    public Searcher(HttpAsyncClient httpClient, QuerierProvider querierProvider) {
        this.querierProvider = checkNotNull(querierProvider);
        this.client = new ProtoBufferHttpClient(httpClient);
    }

    public ListenableFuture<SearchResponse> search(Query query, int page, int perPage, List<String> resources) {
        checkArgument(page > 0, "page should be positive");
        checkArgument(perPage > 0, "perPage should be positive");
        checkNotNull(query, "query can't be null");

        SearchRequest.Builder builder = SearchRequest.newBuilder()
                .setPage(page).setPerPage(perPage).setQuery(query);

        if (resources!=null) {
            // 构建查询ProtoBuf对象
            builder.addAllResources(resources);
        }

        SearchRequest search = builder.build();

        // 获取Querier服务器地址
        QuerierInfo querierInfo;
        try {
            querierInfo = querierProvider.get();
        } catch (QuerierNotFoundException e) {
            SettableFuture<SearchResponse> future = SettableFuture.create();
            future.setException(e);
            return future;
        }

        logger.debug("use querier server: {}", querierInfo);

        return client.execute(querierInfo.getAddr(), search, SearchResponse.PARSER);
    }
}
