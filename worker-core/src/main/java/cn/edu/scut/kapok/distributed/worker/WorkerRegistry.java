package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode.Mode;
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
            @Named(WorkerPropertyNames.WORKDER_NAME) String workerName,
            @Named(WorkerPropertyNames.WORKDER_UUID) String workerUUID,
            @Named(WorkerPropertyNames.WORKDER_ADDR) String workerAddr,
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
    // The service creates a node in zk for the worker, and maintains its state.
    // The node stores the information about the worker.
    public void start() {
        // Build protobuf message.
        WorkerInfo info = createWorkerInfo();

        node = createNode(cf, Mode.PROTECTED_EPHEMERAL_SEQUENTIAL, ZKPath.WORKERS + "/instance-", info);
        node.start();
        logger.info("worker node: {}", info.toString().replace("\n", " "));
    }

    private WorkerInfo createWorkerInfo() {
        return WorkerInfo.newBuilder().setName(workerName)
                .setUuid(workerUUID)
                .setAddr(workerAddr).build();
    }

    PersistentEphemeralNode createNode(CuratorFramework cf, Mode mode, String path, WorkerInfo info) {
        return new PersistentEphemeralNode(cf,
                mode,
                path,
                info.toByteArray());
    }

    // Close the node in zk.
    // The node is deleted after being closed.
    public void close() throws IOException {
        node.close();
    }
}