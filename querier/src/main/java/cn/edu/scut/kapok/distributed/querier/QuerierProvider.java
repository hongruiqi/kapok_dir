package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import static com.google.common.base.Preconditions.*;

public class QuerierProvider {

    private static final Logger logger = LoggerFactory.getLogger(QuerierProvider.class);
    private final CuratorFramework cf;
    private final String querierAddr;
    private PersistentEphemeralNode node;

    // cf is used to communicate with ZooKeeper.
    // querierAddr is the tcp address that others used to
    // connect the querier.
    public QuerierProvider(String querierAddr, CuratorFramework cf) {
        this.cf = checkNotNull(cf);
        this.querierAddr = checkNotNull(querierAddr);
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            // ignored intended
        }
        return null;
    }

    // Return the querierAddr provided by this provider.
    public String getQuerierAddr() {
        return querierAddr;
    }

    public void start() {
        // Build protobuf message.
        QuerierInfo.Builder builder = QuerierInfo.newBuilder();
        builder.setAddr(querierAddr);
        String hostname = resolveHostname();
        if (hostname != null) {
            builder.setHostname(hostname);
        }
        final QuerierInfo info = builder.build();

        // Create the querier node, and start event loop.
        node = new PersistentEphemeralNode(cf,
                PersistentEphemeralNode.Mode.PROTECTED_EPHEMERAL_SEQUENTIAL,
                ZKPath.QUERIERS + "/instance-",
                info.toByteArray());
        node.start();
        logger.info("create querier node: {}", info.toString().replace("\n", " "));
    }

    public void close() throws IOException {
        node.close();
    }
}
