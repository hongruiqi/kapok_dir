package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class QuerierRegistry {

    private static final Logger logger = LoggerFactory.getLogger(QuerierRegistry.class);

    private final CuratorFramework cf; // used to communicate with zk.
    private final String querierAddr; // listening address of querier server.
    private PersistentEphemeralNode node; // zk node for registry.

    @Inject
    public QuerierRegistry(
            @Named("querier.addr") String querierAddr,
            CuratorFramework cf) {
        this.cf = checkNotNull(cf);
        this.querierAddr = checkNotNull(querierAddr);
    }

    // Return the hostname of the machine.
    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            return null;
        }
    }

    // Start the registry service.
    // The service creates a node in zk for the querier, and maintains its state.
    // The node stores the information about the querier.
    public void start() {
        QuerierInfo info = createQuerierInfo();

        node = createNode(info);
        node.start();
        logger.info("create querier node: {}", info.toString().replace("\n", " "));
    }

    private QuerierInfo createQuerierInfo() {
        // Build protobuf message.
        QuerierInfo.Builder builder = QuerierInfo.newBuilder();
        builder.setAddr(querierAddr);
        String hostname = resolveHostname();
        // Optional set the hostname if exsits.
        if (hostname != null) {
            builder.setHostname(hostname);
        }
        return builder.build();
    }

    PersistentEphemeralNode createNode(QuerierInfo info) {
        // Create the querier node, and start event loop.
        node = new PersistentEphemeralNode(cf,
                PersistentEphemeralNode.Mode.PROTECTED_EPHEMERAL_SEQUENTIAL,
                ZKPath.QUERIERS + "/instance-",
                info.toByteArray());
        return node;
    }

    // Close the node in zk.
    // The node is deleted after being closed.
    public void close() throws IOException {
        node.close();
    }
}
