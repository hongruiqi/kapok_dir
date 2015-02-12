package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class WorkerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WorkerRegistry.class);

    private final CuratorFramework cf;
    private final String workerName;
    private final String workerUUID;
    private final String workerAddr;
    private PersistentEphemeralNode node;

    @Inject
    public WorkerRegistry(
            @Named("worker.Name") String workerName,
            @Named("worker.UUID") String workerUUID,
            @Named("worker.Addr") String workerAddr,
            CuratorFramework cf) {
        this.workerName = checkNotNull(workerName);
        this.workerUUID = checkNotNull(workerUUID);
        this.workerAddr = checkNotNull(workerAddr);
        this.cf = checkNotNull(cf);
    }

    // Return the workerAddr provided.
    public String getWorkerAddr() {
        return workerAddr;
    }

    // Start the registry service.
    // The service creates a node in ZooKeeper for the worker, and maintains its state.
    // The node stores the information about the worker.
    public void start() {
        // Build protobuf message.
        final WorkerInfo info = WorkerInfo.newBuilder().setName(workerName)
                .setUuid(workerUUID)
                .setAddr(workerAddr).build();

        // Create the worker node, and start event loop.
        node = new PersistentEphemeralNode(cf,
                PersistentEphemeralNode.Mode.PROTECTED_EPHEMERAL_SEQUENTIAL,
                ZKPath.WORKERS + "/instance-",
                info.toByteArray());
        node.start();
        logger.info("worker node: {}", info.toString().replace("\n", " "));
    }

    // Close the node in ZooKeeper.
    // The node is deleted after being closed.
    public void close() throws IOException {
        node.close();
    }
}
