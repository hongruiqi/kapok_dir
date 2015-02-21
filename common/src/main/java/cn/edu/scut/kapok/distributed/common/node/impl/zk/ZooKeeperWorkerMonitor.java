package cn.edu.scut.kapok.distributed.common.node.impl.zk;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.common.node.WorkerMonitor;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZooKeeperWorkerMonitor monitored the status of the workers.
 */
@Singleton
public class ZooKeeperWorkerMonitor implements WorkerMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperWorkerMonitor.class);

    private final NodeMonitor<WorkerInfo> nodeMonitor;
    private ConcurrentHashMap<String, WorkerInfo> workers = new ConcurrentHashMap<>();

    /**
     * Create a ZooKeeperWorkerMonitor.
     *
     * @param cf used to communicate with ZooKeeper.
     */
    @Inject
    public ZooKeeperWorkerMonitor(CuratorFramework cf) {
        nodeMonitor = new NodeMonitor<>(ZKPath.WORKERS, WorkerInfo.PARSER, this.new EventListener(), cf);
    }

    /**
     * Start the monitor.
     *
     * @throws Exception errors.
     */
    public void start() throws Exception {
        nodeMonitor.start();
    }

    /**
     * Close the monitor.
     *
     * @throws IOException errors
     */
    public void close() throws IOException {
        nodeMonitor.close();
    }

    /**
     * Returns current workers.
     *
     * @return a local copy of the workers.
     */
    @Override
    public ImmutableMap<String, WorkerInfo> getWorkers() {
        return ImmutableMap.copyOf(workers);
    }

    // change workers mapping according to event.
    private class EventListener implements NodeEventListener<WorkerInfo> {
        @Override
        public void onAdd(WorkerInfo nodeInfo) {
            logger.info("worker add: {}", nodeInfo);
            workers.put(nodeInfo.getUuid(), nodeInfo);
        }

        @Override
        public void onUpdate(WorkerInfo nodeInfo) {
            logger.info("worker update: {}", nodeInfo);
            workers.put(nodeInfo.getUuid(), nodeInfo);
        }

        @Override
        public void onRemove(WorkerInfo nodeInfo) {
            logger.info("worker remove: {}", nodeInfo);
            workers.remove(nodeInfo.getUuid());
        }
    }
}
