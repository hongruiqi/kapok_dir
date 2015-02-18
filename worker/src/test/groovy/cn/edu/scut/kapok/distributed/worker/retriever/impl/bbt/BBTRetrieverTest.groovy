package cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt

import cn.edu.scut.kapok.distributed.worker.retriever.spi.RetrieveException
import com.google.common.collect.Lists
import com.google.common.io.ByteStreams
import com.google.common.io.Closeables
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.apache.http.concurrent.FutureCallback
import org.apache.http.nio.client.HttpAsyncClient
import org.junit.Before
import org.junit.Test

import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

import static cn.edu.scut.kapok.distributed.protos.QueryProto.*
import static cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt.BBTRetriever.BBTResult
import static cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt.BBTRetriever.BBTResultList
import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class BBTRetrieverTest {

    HttpAsyncClient httpClient
    byte[] responseContent

    @Before
    void setUp() {
        httpClient = mock(HttpAsyncClient.class)
        def ins = this.getClass().getClassLoader().getResourceAsStream("BBTResult.html");
        assert ins != null
        try {
            responseContent = ByteStreams.toByteArray(ins)
        } finally {
            Closeables.closeQuietly(ins)
        }
    }

    @Test
    void testExtractResult() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def results = bbtRetriever.extractResult(new ByteArrayInputStream(responseContent))
        assert results.getTotal() == 50
        assert results.getResults().size() == 20
    }

    @Test
    void testExtractNotConditon() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def query = Query.newBuilder();
        def words
        query.getWordQueryBuilder().setWord("华工")
        query.setNot(true)
        words = bbtRetriever.extractWords(query.build())
        assert words.toArray() == []
        query.setNot(false)
        words = bbtRetriever.extractWords(query.build())
        assert words.toArray() == ["华工"]
    }

    @Test
    void testExtractEmptyQuery() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def query = Query.newBuilder();
        def words
        words = bbtRetriever.extractWords(query.build())
        assert words.toArray() == []
    }

    @Test
    void testExtractSingleWord() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def query = Query.newBuilder();
        query.getWordQueryBuilder().setWord("华工")
        def words = bbtRetriever.extractWords(query.build())
        assert ['华工'] == words.toArray()
    }

    @Test
    void testExtractMultiWords() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def query = Query.newBuilder();
        def bQueryBuilder = query.getBooleanQueryBuilder()
        bQueryBuilder.addQueriesBuilder().getWordQueryBuilder().setWord("华工")
        bQueryBuilder.addQueriesBuilder().getWordQueryBuilder().setWord("中大")
        bQueryBuilder.setLogic(BooleanQuery.Logic.AND)
        def words = bbtRetriever.extractWords(query.build())
        assert ['华工', '中大'], words.toArray()
    }

    private QueryRequest makeQueryRequest(int from, int count) {
        def builder = QueryRequest.newBuilder().setFrom(from).setCount(count)
        builder.getQueryBuilder().getWordQueryBuilder().setWord("华工")
        return builder.build()
    }

    private HttpAsyncClient makeMockedHttpClient(int statusCode) {
        def httpClient = mock(HttpAsyncClient.class)
        when(httpClient.execute(
                any(HttpGet.class),
                any(FutureCallback.class)))
                .thenAnswer({ invocation ->
            def futureCallback = invocation.getArgumentAt(1, FutureCallback.class)
            def response = mock(HttpResponse.class)
            def statusLine = mock(StatusLine.class)
            def entity = mock(HttpEntity.class)
            when(response.getStatusLine()).thenReturn(statusLine)
            when(statusLine.getStatusCode()).thenReturn(statusCode)
            when(response.getEntity()).thenReturn(entity)
            when(entity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]))
            futureCallback.completed(response)
            return null
        }
        )
        return httpClient
    }

    private HttpAsyncClient makeMockedFailedHttpClient() {
        def httpClient = mock(HttpAsyncClient.class)
        when(httpClient.execute(
                any(HttpGet.class),
                any(FutureCallback.class))).thenAnswer({ invocation ->
            def futureCallback = invocation.getArgumentAt(1, FutureCallback.class)
            futureCallback.failed(new Exception("client failure"));
            return null
        }
        )
        return httpClient
    }

    private HttpAsyncClient makeMockedCancelledHttpClient() {
        def httpClient = mock(HttpAsyncClient.class)
        when(httpClient.execute(any(HttpGet.class), any(FutureCallback.class))).thenAnswer({ invocation ->
            def futureCallback = invocation.getArgumentAt(1, FutureCallback.class)
            futureCallback.cancelled()
            return null
        })
        return httpClient
    }

    @Test
    void testRetrieveOK() {
        def httpClient = makeMockedHttpClient(200)
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))
        doReturn(new BBTResultList()).when(bbtRetriever).extractResult(any(InputStream.class))
        doReturn(QueryResponse.defaultInstance).when(bbtRetriever).bbtResultsToQueryResponse(any(BBTResultList.class))

        def queryResponse = bbtRetriever.retrieve(makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
        assert queryResponse == QueryResponse.defaultInstance
    }

    @Test(expected = UnexpectedStatusCodeException.class)
    void testRetrieveUnexpectedStatusCodeException() {
        def httpClient = makeMockedHttpClient(500)
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))

        try {
            bbtRetriever.retrieve(makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
        } catch (ExecutionException e) {
            throw e.getCause()
        }
    }

    @Test(expected = RetrieveException.class)
    void testRetrieveExtractResultException() {
        def httpClient = makeMockedHttpClient(200)
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))
        doThrow(new NullPointerException()).when(bbtRetriever).extractResult(any(InputStream.class))

        try {
            bbtRetriever.retrieve(makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
        } catch (ExecutionException e) {
            throw e.getCause()
        }
    }

    @Test(expected = RetrieveFailedException.class)
    void testRetrieveFailedException() {
        def httpClient = makeMockedFailedHttpClient()
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))

        try {
            bbtRetriever.retrieve(makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
        } catch (ExecutionException e) {
            throw e.getCause()
        }
    }

    @Test(expected = CancellationException.class)
    void testRetrieveCancelled() {
        def httpClient = makeMockedCancelledHttpClient()
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))

        bbtRetriever.retrieve(makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
    }

    @Test
    void testBBTResultsToQueryResponse() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def results = new BBTResultList()
        results.setTotal(50)
        def result = new BBTResult("http://www.google.com/",
                "Google",
                "Google Search Engine",
                "2015-01-01"
        )
        results.addResult(result)
        def resp = bbtRetriever.bbtResultsToQueryResponse(results)
        assert resp.getTotal() == 50
        assert resp.getResultsCount() == 1
        def fields = resp.getResults(0).getFieldsList()
        assert fields.size() == 4
        fields.each { field ->
            def name = field.getName()
            assert field.getValue() == result[name]
        }
    }
}
