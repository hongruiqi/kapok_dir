package cn.edu.scut.kapok.distributed.test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelegateServletInputStream extends ServletInputStream {

    private final InputStream stream;

    public DelegateServletInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return stream.read(bytes);
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return stream.read(bytes, i, i1);
    }

    @Override
    public long skip(long l) throws IOException {
        return stream.skip(l);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void mark(int i) {
        stream.mark(i);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }
}
