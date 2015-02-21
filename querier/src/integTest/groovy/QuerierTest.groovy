import cn.edu.scut.kapok.distributed.protos.SearchRequest
import cn.edu.scut.kapok.distributed.protos.SearchResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients
import org.junit.Before
import org.junit.Test

class QuerierTest {

    HttpClient httpClient

    @Before
    void setUp() {
        httpClient = HttpClients.createDefault()
    }

    @Test
    void testQuerier() {
        def builder = SearchRequest.newBuilder()
        builder.setPage(1).setPerPage(10)
        builder.getQueryBuilder().getWordQueryBuilder().setWord("华工")
        def url = "http://127.0.0.1:8000/search"
        def post = new HttpPost(url)
        post.setEntity(new ByteArrayEntity(builder.build().toByteArray()))
        def resp = httpClient.execute(post)
        assert resp.getStatusLine().getStatusCode() == 200
        def searchResponse = SearchResponse.parseFrom(resp.getEntity().getContent())
        assert searchResponse.getTotalHit() != 0
        assert searchResponse.getResourceStatsList().size() != 0
    }
}
