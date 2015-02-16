package cn.edu.scut.kapok.distributed.common.node.spi;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto;
import com.google.common.collect.ImmutableMap;

public interface WorkerManager {
    ImmutableMap<String, WorkerInfoProto.WorkerInfo> getWorkers();
}
