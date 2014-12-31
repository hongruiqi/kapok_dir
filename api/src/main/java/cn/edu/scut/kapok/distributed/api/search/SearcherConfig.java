package cn.edu.scut.kapok.distributed.api.search;

public class SearcherConfig {
    public HTTP http;

    public static class HTTP {
        public int connectTimeout;
        public int threadPoolSize;
    }
}
