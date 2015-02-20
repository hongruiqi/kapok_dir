package cn.edu.scut.kapok.distributed.common.httpclient;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.client.HttpAsyncClient;

import java.io.IOException;

public class ProtoBufferHttpClient {

    private final HttpAsyncClient httpClient;

    public ProtoBufferHttpClient(HttpAsyncClient httpClient) {
        this.httpClient = httpClient;
    }

    public <T1 extends MessageLite, T2> ListenableFuture<T2> execute(String uri, T1 request, final Parser<T2> responseParser) {
        final SettableFuture<T2> future = SettableFuture.create();
        HttpPost post = new HttpPost(uri);
        post.setEntity(new ByteArrayEntity(request.toByteArray()));
        httpClient.execute(post, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    future.setException(new Exception("status code error"));
                    return;
                }
                try {
                    T2 response = responseParser.parseFrom(result.getEntity().getContent());
                    future.set(response);
                } catch (IOException e) {
                    future.setException(e);
                }
            }

            @Override
            public void failed(Exception ex) {
                future.setException(ex);
            }

            @Override
            public void cancelled() {
                future.cancel(true);
            }
        });
        return future;
    }
}
