package cn.edu.scut.kapok.distributed.common.http;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ProtoBufferHttpServlet<T1 extends MessageLite, T2 extends MessageLite> extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(ProtoBufferHttpServlet.class);

    private final Parser<T1> requestParser;

    public ProtoBufferHttpServlet(Parser<T1> requestParser) {
        this.requestParser = requestParser;
    }

    @Override
    protected final void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext asyncContext = req.startAsync();

        // Parse request.
        T1 request;
        try (InputStream in = req.getInputStream()) {
            request = requestParser.parseFrom(in);
        } catch (IOException e) {
            logger.debug("parse protobuffer request", e);
            throw e;
        }

        // Process request.
        ListenableFuture<T2> future = process(request);
        checkNotNull(future);

        // Add callback to be called with result.
        Futures.addCallback(future, new FutureCallback<T2>() {
            @Override
            public void onSuccess(T2 result) {
                try (OutputStream out = resp.getOutputStream()) {
                    result.writeTo(out);
                } catch (IOException e) {
                    logger.debug("serialize and write protobuffer response", e);
                } finally {
                    asyncContext.complete();
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable t) {
                logger.error("processing error", t);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                asyncContext.complete();
            }
        });
    }

    protected abstract ListenableFuture<T2> process(T1 request);
}
