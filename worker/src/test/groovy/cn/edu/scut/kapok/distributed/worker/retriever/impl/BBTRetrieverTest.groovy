package cn.edu.scut.kapok.distributed.worker.retriever.impl

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
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import static cn.edu.scut.kapok.distributed.protos.QueryProto.*
import static cn.edu.scut.kapok.distributed.worker.retriever.impl.BBTRetriever.*
import static cn.edu.scut.kapok.distributed.worker.retriever.impl.BBTRetriever.BBTResultList
import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class BBTRetrieverTest extends GroovyTestCase {

    HttpAsyncClient httpClient
    byte[] responseContent

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

    void testExtractResult() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def results = bbtRetriever.extractResult(new ByteArrayInputStream(responseContent))
        assert results.getTotal() == 50
        assert results.getResults().size() == 20
    }

    void testExtractSingleWord() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def query = Query.newBuilder();
        query.getWordQueryBuilder().setWord("华工")
        def words = bbtRetriever.extractWords(query.build())
        assertArrayEquals(['华工'] as String[], words.toArray())
    }

    void testExtractMultiWords() {
        def bbtRetriever = new BBTRetriever(httpClient)
        def query = Query.newBuilder();
        def bQueryBuilder = query.getBooleanQueryBuilder()
        bQueryBuilder.addQueriesBuilder().getWordQueryBuilder().setWord("华工")
        bQueryBuilder.addQueriesBuilder().getWordQueryBuilder().setWord("中大")
        bQueryBuilder.setLogic(BooleanQuery.Logic.AND)
        def words = bbtRetriever.extractWords(query.build())
        assertArrayEquals(['华工', '中大'] as String[], words.toArray())
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
                any(FutureCallback.class))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
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
        })
        return httpClient
    }

    private HttpAsyncClient makeMockedFailHttpClient() {
        def httpClient = mock(HttpAsyncClient.class)
        when(httpClient.execute(
                any(HttpGet.class),
                any(FutureCallback.class))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                def futureCallback = invocation.getArgumentAt(1, FutureCallback.class)
                futureCallback.failed(new Exception("client failure"));
                return null
            }
        })
        return httpClient
    }

    void testRetrieveOK() {
        def httpClient = makeMockedHttpClient(200)
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))
        doReturn(new BBTResultList()).when(bbtRetriever).extractResult(any(InputStream.class))
        doReturn(QueryResponse.defaultInstance).when(bbtRetriever).bbtResultsToQueryResponse(any(BBTResultList.class))

        def queryResponse
        try {
            queryResponse = bbtRetriever.retrieve(
                    makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
        } catch (TimeoutException e) {
            fail("response is not set.")
            return;
        }
        assert queryResponse == QueryResponse.defaultInstance
    }

    void testRetrieveStatusCodeError() {
        def httpClient = makeMockedHttpClient(500)
        testRetrieveError(httpClient)
    }

    void testRetrieveInternalError() {
        def httpClient = makeMockedFailHttpClient()
        testRetrieveError(httpClient)
    }

    private void testRetrieveError(HttpAsyncClient httpClient) {
        def bbtRetriever = spy(new BBTRetriever(httpClient))
        doReturn(Lists.newArrayList("华工")).when(bbtRetriever).extractWords(any(Query.class))

        try {
            bbtRetriever.retrieve(
                    makeQueryRequest(0, 10)).get(1, TimeUnit.SECONDS)
            fail("should throw RetrieveException.")
        } catch (TimeoutException e) {
            fail("can't get response.")
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RetrieveException) {
                // expected.
                return;
            }
            throw e;
        }
    }

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
        for (def field: fields) {
            def name = field.getName()
            assert field.getValue() == result[name]
        }
    }
}
