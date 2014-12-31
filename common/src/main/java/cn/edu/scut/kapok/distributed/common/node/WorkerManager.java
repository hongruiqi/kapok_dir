package cn.edu.scut.kapok.distributed.common.node;

import cn.edu.scut.kapok.distributed.common.ProtoParser;
import cn.edu.scut.kapok.distributed.common.ZKPath;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// WorkerManager manages the status of the workers.
// start() should be called to make the manager working.
public class WorkerManager {
    private static final Logger logger = LoggerFactory.getLogger(WorkerManager.class);

    private final NodeManager<WorkerInfo> nodeManager;
    private ConcurrentHashMap<String, WorkerInfo> workers = new ConcurrentHashMap<>();

    public WorkerManager(CuratorFramework cf) {
        nodeManager = new NodeManager<>(ZKPath.WORKERS, new ProtoParser<WorkerInfo>() {
            @Override
            public WorkerInfo parseFrom(byte[] msg) throws InvalidProtocolBufferException {
                return WorkerInfo.parseFrom(msg);
            }
        }, this.new EventListener(), cf);
    }

    public void start() throws Exception {
        nodeManager.start();
    }

    public void close() throws IOException {
        nodeManager.close();
    }

    public ImmutableMap<String, WorkerInfo> getWorkers() {
        return ImmutableMap.copyOf(workers);
    }

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
