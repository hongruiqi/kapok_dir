import cn.edu.scut.kapok.distributed.api.search.Searcher
import cn.edu.scut.kapok.distributed.api.search.querier.provider.QuerierProvider
import cn.edu.scut.kapok.distributed.api.search.querier.provider.SingleQuerierProvider
import cn.edu.scut.kapok.distributed.protos.QuerierInfo
import cn.edu.scut.kapok.distributed.protos.Query
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.concurrent.TimeUnit

class SearcherTest {

    CloseableHttpAsyncClient httpClient
    QuerierProvider querierProvider

    @Before
    void setUp() {
        httpClient = HttpAsyncClients.createDefault()
        httpClient.start()
        def querierInfo = QuerierInfo.newBuilder()
                .setAddr("http://127.0.0.1:8000/search")
                .setHostname("HRQ-PC").build()
        querierProvider = new SingleQuerierProvider(querierInfo)
    }

    @After
    void tearDown() {
        httpClient.close()
    }

    @Test
    void testSearcher() {
        def searcher = new Searcher(httpClient, querierProvider)
        def builder = Query.newBuilder();
        builder.getWordQueryBuilder().setWord("华工");
        def query = builder.build()
        def future = searcher.search(query, 1, 10, null)
        def resp = future.get(5, TimeUnit.SECONDS)
        assert resp.getTotalHit() != 0
    }
}
