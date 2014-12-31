package cn.edu.scut.kapok.distributed.querier;

import com.google.common.base.MoreObjects;

public class QuerierConfig {
    public Server Server;

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Server", Server)
                .toString();
    }

    // Configuration for the Querier server.
    public static class Server {
        public String Host;
        public int Port;

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("Host", Host)
                    .add("Port", Port)
                    .toString();
        }
    }
}
