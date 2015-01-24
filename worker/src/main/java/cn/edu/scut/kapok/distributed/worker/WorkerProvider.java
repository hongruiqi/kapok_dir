package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.*;

public class WorkerProvider {

    private static final Logger logger = LoggerFactory.getLogger(WorkerProvider.class);

    private final CuratorFramework cf;
    private final String workerAddr;
    private PersistentEphemeralNode node;

    // cf is used to communicate with ZooKeeper.
    // workerAddr is the tcp address that others used to
    // connect the worker.
    public WorkerProvider(String workerAddr, CuratorFramework cf) {
        this.cf = checkNotNull(cf);
        this.workerAddr = checkNotNull(workerAddr);
    }

    // Return the workerAddr provided.
    public String getWorkerAddr() {
        return workerAddr;
    }

    public void start() {
        // Build protobuf message.
        WorkerInfo.Builder builder = WorkerInfo.newBuilder();
        builder.setAddr(workerAddr);

        final WorkerInfo info = builder.build();

        // Create the querier node, and start event loop.
        node = new PersistentEphemeralNode(cf,
                PersistentEphemeralNode.Mode.PROTECTED_EPHEMERAL_SEQUENTIAL,
                ZKPath.WORKERS + "/instance-",
                info.toByteArray());
        node.start();
        logger.info("create worker node: {}", info.toString().replace("\n", " "));
    }

    public void close() throws IOException {
        node.close();
    }
}
