package cn.edu.scut.kapok.distributed.querier.search.impl.fetch
import cn.edu.scut.kapok.distributed.querier.api.search.fetch.FetchException
import com.google.common.io.ByteStreams
import com.google.common.util.concurrent.SettableFuture
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.concurrent.FutureCallback
import org.apache.http.nio.client.HttpAsyncClient
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest
import static cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse
import static cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo
import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class SimpleFetcherTest {

    HttpAsyncClient httpClient
    SimpleFetcher fetcher
    SettableFuture<QueryResponse> future
    WorkerInfo workerInfo
    String workerName = "TestWorker"
    String workerUuid = "TestUUID"
    String workerAddr = "http://127.0.0.1:10001/search"
    QueryRequest queryRequest
    QueryResponse queryResponse

    @Before
    void setUp() {
        httpClient = mock(HttpAsyncClient.class)
        fetcher = spy(new SimpleFetcher(httpClient))
        future = SettableFuture.create()
        workerInfo = WorkerInfo.newBuilder()
                .setName(workerName)
                .setUuid(workerUuid)
                .setAddr(workerAddr).build()
        def builder = QueryRequest.newBuilder().setFrom(0).setCount(10)
        builder.getQueryBuilder().getWordQueryBuilder().setWord("华工")
        queryRequest = builder.build()
        queryResponse = QueryResponse.newBuilder().setTotal(10).build()
    }

    @Test
    void testSimpleFetchExecute() {
        doReturn(future).when(fetcher).createFuture()
        doReturn(mock(Future.class)).when(httpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class))
        fetcher.fetch(workerInfo, queryRequest)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        def request = requestCaptor.value
        assert request.getURI().toString() == workerAddr
        assert ByteStreams.toByteArray(request.entity.content) == queryRequest.toByteArray()
    }

    private FutureCallback testSimpleFetchCallback() {
        doReturn(future).when(fetcher).createFuture()
        doReturn(mock(Future.class)).when(httpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class))
        fetcher.fetch(workerInfo, queryRequest)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        return callbackCaptor.value
    }

    @Test
    void testSimpleFetchSuccess() {
        def callback = testSimpleFetchCallback()
        def result = mock(HttpResponse.class)
        def statusLine = mock(StatusLine.class)
        when(result.getStatusLine()).thenReturn(statusLine)
        when(statusLine.getStatusCode()).thenReturn(200)
        def entity = mock(HttpEntity.class)
        when(result.getEntity()).thenReturn(entity)
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(queryResponse.toByteArray()))
        callback.completed(result)
        def qResponse = future.get(1, TimeUnit.SECONDS)
        assert qResponse.toByteArray() == queryResponse.toByteArray()
    }

    @Test(expected = FetchException.class)
    void testSimpleFetchStatusCodeError() {
        def callback = testSimpleFetchCallback()
        def result = mock(HttpResponse.class)
        def statusLine = mock(StatusLine.class)
        when(result.getStatusLine()).thenReturn(statusLine)
        when(statusLine.getStatusCode()).thenReturn(500)
        callback.completed(result)
        verify(result).getStatusLine()
        verifyNoMoreInteractions(result)
        try {
            assert future.isDone()
            future.get()
        } catch (ExecutionException e) {
            throw e.getCause()
        }
    }

    @Test(expected = IOException.class)
    void testSimpleFetchParseResponseError() {
        def callback = testSimpleFetchCallback()
        def result = mock(HttpResponse.class)
        def statusLine = mock(StatusLine.class)
        when(result.getStatusLine()).thenReturn(statusLine)
        when(statusLine.getStatusCode()).thenReturn(200)
        def entity = mock(HttpEntity.class)
        when(result.getEntity()).thenReturn(entity)
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(new byte[1]))
        callback.completed(result)
        verify(result).getStatusLine()
        verify(result).getEntity()
        verify(entity).getContent()
        try {
            assert future.isDone()
            future.get()
        } catch (ExecutionException e) {
            def cause = e.getCause()
            assert cause instanceof FetchException
            throw cause.getCause()
        }
    }

    @Test(expected = FetchException.class)
    void testSimpleFetchFailed() {
        def callback = testSimpleFetchCallback()
        callback.failed(new Exception())
        try {
            assert future.isDone()
            future.get()
        } catch (ExecutionException e) {
            throw e.getCause()
        }
    }

    @Test(expected = FetchException.class)
    void testSimpleFetchCancelled() {
        def callback = testSimpleFetchCallback()
        callback.cancelled()
        try {
            assert future.isDone()
            future.get()
        } catch (ExecutionException e) {
            throw e.getCause()
        }
    }
}
