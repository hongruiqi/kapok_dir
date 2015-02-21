package cn.edu.scut.kapok.distributed.worker.servlet
import cn.edu.scut.kapok.distributed.protos.QueryRequest
import cn.edu.scut.kapok.distributed.protos.QueryResponse
import cn.edu.scut.kapok.distributed.test.ByteArrayServletOutputStream
import cn.edu.scut.kapok.distributed.test.DelegateServletInputStream
import cn.edu.scut.kapok.distributed.worker.api.retriever.RetrieveException
import cn.edu.scut.kapok.distributed.worker.api.retriever.Retriever
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.ListenableFuture
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyInt
import static org.mockito.Mockito.*

class SearchServletTest {
    Retriever retriever
    SearchServlet servlet
    HttpServletRequest request
    HttpServletResponse response
    ListenableFuture future
    AsyncContext context
    QueryRequest queryRequest
    QueryResponse queryResponse

    @Before
    void setUp() {
        retriever = mock(Retriever.class)
        servlet = spy(new SearchServlet(retriever))
        request = mock(HttpServletRequest.class)
        response = mock(HttpServletResponse.class)
        future = mock(ListenableFuture.class)
        context = mock(AsyncContext.class)
        when(request.startAsync()).thenReturn(context)
        doNothing().when(response).sendError(anyInt())
        def builder = QueryRequest.newBuilder().setFrom(0).setCount(10)
        builder.getQueryBuilder().getWordQueryBuilder().setWord("华工")
        queryRequest = builder.build()
        queryResponse = QueryResponse.newBuilder().setTotal(100).build()
    }

    @Test
    void testSearchServlet() {
        def inputStream = new DelegateServletInputStream(
                new ByteArrayInputStream(queryRequest.toByteArray()))
        def outputStream = new ByteArrayServletOutputStream()
        when(request.getInputStream()).thenReturn(inputStream)
        when(response.getOutputStream()).thenReturn(outputStream)
        when(retriever.retrieve(any(QueryRequest.class))).thenReturn(future)
        doNothing().when(servlet).addCallback(any(ListenableFuture.class), any(FutureCallback.class))
        servlet.doPost(request, response)
        verify(request).startAsync()
        verify(request).getInputStream()
        def futureCaptor = ArgumentCaptor.forClass(ListenableFuture.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(servlet).addCallback(futureCaptor.capture(), callbackCaptor.capture())
        callbackCaptor.value.onSuccess(queryResponse)
        verify(response).getOutputStream()
        verify(context).complete()
        assert outputStream.toByteArray() == queryResponse.toByteArray()
    }

    @Test
    void testBadInput() {
        def inputStream = new DelegateServletInputStream(
                new ByteArrayInputStream(QueryRequest.defaultInstance.toByteArray()))
        when(request.getInputStream()).thenReturn(inputStream)
        servlet.doPost(request, response)
        verify(request).getInputStream()
        verify(response).sendError(400)
    }

    @Test
    void testRetrieveFail() {
        def inputStream = new DelegateServletInputStream(
                new ByteArrayInputStream(queryRequest.toByteArray()))
        when(request.getInputStream()).thenReturn(inputStream)
        when(retriever.retrieve(any(QueryRequest.class))).thenReturn(future)
        doNothing().when(servlet).addCallback(any(ListenableFuture.class), any(FutureCallback.class))
        servlet.doPost(request, response)
        verify(request).startAsync()
        verify(request).getInputStream()
        def futureCaptor = ArgumentCaptor.forClass(ListenableFuture.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(servlet).addCallback(futureCaptor.capture(), callbackCaptor.capture())
        callbackCaptor.value.onFailure(new RetrieveException())
        verify(response).sendError(500)
        verify(context).complete()
    }
}
