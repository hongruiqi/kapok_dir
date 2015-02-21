import cn.edu.scut.kapok.distributed.protos.QueryRequest
import cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt.BBTRetriever
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.concurrent.TimeUnit

class BBTRetrieverTest {

    CloseableHttpAsyncClient httpClient
    BBTRetriever bbtRetriever

    @Before
    void setUp() {
        httpClient = HttpAsyncClients.createDefault()
        httpClient.start()
        bbtRetriever = new BBTRetriever(httpClient)
    }

    @After
    void shutdown() {
        httpClient.close()
    }

    @Test
    void testRetriever() {
        def builder = QueryRequest.newBuilder().setFrom(0).setCount(10)
        builder.getQueryBuilder().getWordQueryBuilder().setWord("华工")
        def request = builder.build()
        def future = bbtRetriever.retrieve(request)
        def response = future.get(5, TimeUnit.SECONDS)
        assert response.getTotal() != 0
        assert response.getResultsCount() != 0
    }
}
