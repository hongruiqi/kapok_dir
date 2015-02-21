package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
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

/**
 * WorkerRegistry maintains a node in ZooKeeper.
 * The node stores information about the worker.
 */
@Singleton
public class WorkerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WorkerRegistry.class);

    private final CuratorFramework cf;
    private final String workerName;
    private final String workerUuid;
    private final String workerAddr;
    private final String workerPath;
    private PersistentEphemeralNode node;

    /**
     * Create worker registry instance.
     * {@code workerAddr} and {@code workerPath} are combinded to form
     * the address of the worker.
     *
     * @param workerName Name of the worker.
     * @param workerUuid UUID of the worker.
     * @param workerAddr Addr of the worker.
     * @param workerPath Path of the worker.
     * @param cf         Handle used to communicate with ZooKeeper.
     */
    @Inject
    public WorkerRegistry(
            @Named(WorkerPropertyNames.WORKER_NAME) String workerName,
            @Named(WorkerPropertyNames.WORKER_UUID) String workerUuid,
            @Named(WorkerPropertyNames.WORKER_ADDR) String workerAddr,
            @Named(WorkerPropertyNames.WORKER_PATH) String workerPath,
            CuratorFramework cf) {
        this.workerName = checkNotNull(workerName);
        this.workerUuid = checkNotNull(workerUuid);
        this.workerAddr = checkNotNull(workerAddr);
        this.workerPath = checkNotNull(workerPath);
        this.cf = checkNotNull(cf);
    }

    /**
     * Start the registry.
     */
    public void start() {
        WorkerInfo info = createWorkerInfo();

        node = createNode(Mode.PROTECTED_EPHEMERAL_SEQUENTIAL, ZKPath.WORKERS + "/instance-", info);
        node.start();
        logger.info("worker node: {}", info.toString().replace("\n", " "));
    }

    /**
     * Build protobuf object with worker infomation.
     *
     * @return Generated {@code WorkerInfo}.
     */
    private WorkerInfo createWorkerInfo() {
        return WorkerInfo.newBuilder().setName(workerName)
                .setUuid(workerUuid)
                .setAddr(String.format("http://%s%s", workerAddr, workerPath)).build();
    }

    /**
     * Create a @{code PersistentEphemeralNode}.
     *
     * @param mode Node Mode.
     * @param path Node path.
     * @param info {@code WorkerInfo} to be stored in node.
     * @return Created node.
     */
    PersistentEphemeralNode createNode(Mode mode, String path, WorkerInfo info) {
        return new PersistentEphemeralNode(cf,
                mode,
                path,
                info.toByteArray());
    }

    /**
     * Close the registry. The node registed is also deleted.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        node.close();
    }
}
