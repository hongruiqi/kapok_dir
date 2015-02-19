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
    private final String workerPath;
    private PersistentEphemeralNode node;

    @Inject
    public WorkerRegistry(
            @Named(WorkerPropertyNames.WORKER_NAME) String workerName,
            @Named(WorkerPropertyNames.WORKER_UUID) String workerUUID,
            @Named(WorkerPropertyNames.WORKER_ADDR) String workerAddr,
            @Named(WorkerPropertyNames.WORKER_PATH) String workerPath,
            CuratorFramework cf) {
        this.workerName = checkNotNull(workerName);
        this.workerUUID = checkNotNull(workerUUID);
        this.workerAddr = checkNotNull(workerAddr);
        this.workerPath = checkNotNull(workerPath);
        this.cf = checkNotNull(cf);
    }

    // Start the registry service.
    // The service creates a node in zk for the worker, and maintains its state.
    // The node stores the information about the worker.
    public void start() {
        // Build protobuf message.
        WorkerInfo info = createWorkerInfo();

        node = createNode(Mode.PROTECTED_EPHEMERAL_SEQUENTIAL, ZKPath.WORKERS + "/instance-", info);
        node.start();
        logger.info("worker node: {}", info.toString().replace("\n", " "));
    }

    private WorkerInfo createWorkerInfo() {
        return WorkerInfo.newBuilder().setName(workerName)
                .setUuid(workerUUID)
                .setAddr(String.format("http://%s%s", workerAddr, workerPath)).build();
    }

    PersistentEphemeralNode createNode(Mode mode, String path, WorkerInfo info) {
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
