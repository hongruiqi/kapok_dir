import cn.edu.scut.kapok.distributed.protos.QueryRequest
import cn.edu.scut.kapok.distributed.protos.QueryResponse
import cn.edu.scut.kapok.distributed.worker.api.retriever.Retriever
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients
import org.junit.Before
import org.junit.Test

class WorkerTest {

    HttpClient httpClient
    Retriever retriever

    @Before
    void setUp() {
        httpClient = HttpClients.createDefault()
    }

    @Test
    void testWorker() {
        def builder = QueryRequest.newBuilder().setFrom(0).setCount(10)
        builder.getQueryBuilder().getWordQueryBuilder().setWord("华工")
        def url = "http://127.0.0.1:10001/search"
        def post = new HttpPost(url)
        post.setEntity(new ByteArrayEntity(builder.build().toByteArray()))
        def resp = httpClient.execute(post)
        assert resp.getStatusLine().getStatusCode() == 200
        def queryResponse = QueryResponse.parseFrom(resp.getEntity().getContent())
        assert queryResponse.getTotal() != 0
        assert queryResponse.getResultsCount() != 0
    }
}
