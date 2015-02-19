package cn.edu.scut.kapok.distributed.common.node.impl.zk;

import cn.edu.scut.kapok.distributed.common.ProtoParser;
import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.common.node.QuerierManager;
import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// QuerierManager manages the status of the queriers.
// start() should be called to make the manager working.
@Singleton
public class ZooKeeperQuerierManager implements QuerierManager {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperQuerierManager.class);

    private NodeManager<QuerierInfo> nodeManager;
    private ConcurrentHashMap<String, QuerierInfo> queriers = new ConcurrentHashMap<>();

    @Inject
    public ZooKeeperQuerierManager(CuratorFramework cf) {
        nodeManager = new NodeManager<>(ZKPath.QUERIERS, new ProtoParser<QuerierInfo>() {
            @Override
            public QuerierInfo parseFrom(byte[] msg) throws InvalidProtocolBufferException {
                return QuerierInfo.parseFrom(msg);
            }
        }, this.new EventListener(), cf);
    }

    public void start() throws Exception {
        nodeManager.start();
    }

    public void close() throws IOException {
        nodeManager.close();
    }

    @Override
    public ImmutableMap<String, QuerierInfo> getQueriers() {
        return ImmutableMap.copyOf(queriers);
    }

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
