package cn.edu.scut.kapok.distributed.test;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class ByteArrayServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Override
    public void write(byte[] bytes, int i, int i1) {
        outputStream.write(bytes, i, i1);
    }

    @Override
    public void write(int i) {
        outputStream.write(i);
    }

    public byte[] toByteArray() {
        return outputStream.toByteArray();
    }

    public void reset() {
        outputStream.reset();
    }

    public int size() {
        return outputStream.size();
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        this.outputStream.writeTo(outputStream);
    }

    @Override
    public String toString() {
        return outputStream.toString();
    }

    public String toString(String s) throws UnsupportedEncodingException {
        return outputStream.toString(s);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public boolean isReady() {
        return false;
    }
}
