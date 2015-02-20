package cn.edu.scut.kapok.distributed.common.httpclient

import com.google.common.io.ByteStreams
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpPost
import org.apache.http.concurrent.FutureCallback
import org.apache.http.nio.client.HttpAsyncClient
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import java.util.concurrent.Future

import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class ProtoBufferHttpClientTest {

    HttpAsyncClient httpClient
    ProtoBufferHttpClient client
    MessageLite reqProto
    MessageLite respProto
    Parser parser
    String uri = "http://192.168.1.1/forTest"
    byte[] dummyBytes = 1..5
    byte[] dummyBytes2 = 5..1
    InputStream contentStream = new ByteArrayInputStream(dummyBytes2)

    @Before
    void setUp() {
        httpClient = mock(HttpAsyncClient.class)
        client = spy(new ProtoBufferHttpClient(httpClient))
        reqProto = mock(MessageLite.class)
        when(reqProto.toByteArray()).thenReturn(dummyBytes)
        respProto = mock(MessageLite.class)
        parser = mock(Parser.class)
        when(parser.parseFrom(contentStream)).thenReturn(respProto)
    }

    @Test
    void testProtoBufferHttpClientExecute() {
        when(httpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenReturn(mock(Future.class))
        client.execute(uri, reqProto, parser)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        verify(reqProto).toByteArray()
        assert requestCaptor.value.getURI().toString() == uri
        assert ByteStreams.toByteArray(requestCaptor.value.getEntity().getContent()) == dummyBytes
    }

    @Test
    void testProtoBufferHttpClientCallbackSuccess() {
        when(httpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenReturn(mock(Future.class))
        def future = client.execute(uri, reqProto, parser)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        def callback = callbackCaptor.value
        def result = mock(HttpResponse.class)
        def statusLine = mock(StatusLine.class)
        def entity = mock(HttpEntity.class)
        when(result.getStatusLine()).thenReturn(statusLine)
        when(statusLine.getStatusCode()).thenReturn(200)
        when(result.getEntity()).thenReturn(entity)
        when(entity.getContent()).thenReturn(contentStream)
        callback.completed(result)
        verify(parser).parseFrom(contentStream)
        assert future.isDone()
        assert future.get() == respProto
    }

    @Test(expected = Exception.class)
    void testProtoBufferHttpClientCallbackStatusError() {
        when(httpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenReturn(mock(Future.class))
        def future = client.execute(uri, reqProto, parser)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        def callback = callbackCaptor.value
        def result = mock(HttpResponse.class)
        def statusLine = mock(StatusLine.class)
        when(result.getStatusLine()).thenReturn(statusLine)
        when(statusLine.getStatusCode()).thenReturn(500)
        verifyNoMoreInteractions(result)
        verifyZeroInteractions(parser)
        callback.completed(result)
        assert future.isDone()
        future.get()
    }

    @Test(expected = Exception.class)
    void testProtoBufferHttpClientCallbackFailed() {
        when(httpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenReturn(mock(Future.class))
        def future = client.execute(uri, reqProto, parser)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        def callback = callbackCaptor.value
        verifyZeroInteractions(parser)
        callback.failed(new Exception())
        assert future.isDone()
        future.get()
    }

    @Test
    void testProtoBufferHttpClientCallbackCancelled() {
        when(httpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenReturn(mock(Future.class))
        def future = client.execute(uri, reqProto, parser)
        def requestCaptor = ArgumentCaptor.forClass(HttpPost.class)
        def callbackCaptor = ArgumentCaptor.forClass(FutureCallback.class)
        verify(httpClient).execute(requestCaptor.capture(), callbackCaptor.capture())
        def callback = callbackCaptor.value
        verifyZeroInteractions(parser)
        callback.cancelled()
        assert future.cancelled
    }
}
