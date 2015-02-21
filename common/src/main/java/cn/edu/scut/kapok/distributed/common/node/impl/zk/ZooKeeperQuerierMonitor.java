package cn.edu.scut.kapok.distributed.common.node.impl.zk;

import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.common.node.QuerierMonitor;
import cn.edu.scut.kapok.distributed.protos.QuerierInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZooKeeperQuerierMonitor monitored the status of the querier node.
 */
@Singleton
public class ZooKeeperQuerierMonitor implements QuerierMonitor {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperQuerierMonitor.class);

    private NodeMonitor<QuerierInfo> nodeMonitor;
    private ConcurrentHashMap<String, QuerierInfo> queriers = new ConcurrentHashMap<>();

    /**
     * Create a ZooKeeerQuerierMonitor.
     *
     * @param cf used to communicate with ZooKeeper.
     */
    @Inject
    public ZooKeeperQuerierMonitor(CuratorFramework cf) {
        nodeMonitor = new NodeMonitor<>(ZKPath.QUERIERS, QuerierInfo.PARSER, this.new EventListener(), cf);
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
     * @throws IOException errors.
     */
    public void close() throws IOException {
        nodeMonitor.close();
    }

    /**
     * Returns current queriers.
     * Results are copy from a local cache,
     * so cost is little.
     *
     * @return current queriers.
     */
    @Override
    public ImmutableMap<String, QuerierInfo> getQueriers() {
        return ImmutableMap.copyOf(queriers);
    }

    // change map with events.
    private class EventListener implements NodeEventListener<QuerierInfo> {
        @Override
        public void onAdd(QuerierInfo nodeInfo) {
            logger.info("querier add: {}", nodeInfo);
            queriers.put(nodeInfo.getAddr(), nodeInfo);
        }

        @Override
        public void onUpdate(QuerierInfo nodeInfo) {
            logger.info("querier update: {}", nodeInfo);
            queriers.put(nodeInfo.getAddr(), nodeInfo);
        }

        @Override
        public void onRemove(QuerierInfo nodeInfo) {
            logger.info("querier remove: {}", nodeInfo);
            queriers.remove(nodeInfo.getAddr());
        }
    }
}
