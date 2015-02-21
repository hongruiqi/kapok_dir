package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.protos.QuerierInfo;
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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * QuerierRegistry maintains registry of the querier.
 * The registry creates and maintains a node in ZooKeeper.
 * Information about querier is stored in the node.
 */
@Singleton
public class QuerierRegistry {

    private static final Logger logger = LoggerFactory.getLogger(QuerierRegistry.class);

    private final CuratorFramework cf; // used to communicate with zk.
    private final String querierAddr; // listening address of querier server.
    private PersistentEphemeralNode node; // zk node for registry.

    /**
     * Create {@code QuerierRegistry} instance.
     *
     * @param querierAddr Address of the querier.
     * @param cf          Handle used to communicate with ZooKeeper.
     */
    @Inject
    public QuerierRegistry(
            @Named("querier.addr") String querierAddr,
            CuratorFramework cf) {
        this.cf = checkNotNull(cf);
        this.querierAddr = checkNotNull(querierAddr);
    }

    // Return the hostname of machine.
    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            return null;
        }
    }

    /**
     * Start the registry.
     */
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

        // Set the hostname if not null.
        String hostname = resolveHostname();
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

    /**
     * Close the registry. The node in ZooKeeper is also deleted.
     *
     * @throws IOException Exception occured when close.
     */
    public void close() throws IOException {
        node.close();
    }
}
