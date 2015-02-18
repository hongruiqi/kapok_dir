package cn.edu.scut.kapok.distributed.common;

public interface HttpServer {
    void start() throws Exception;

    void join() throws InterruptedException;

    void stop() throws Exception;
}
