package cn.edu.scut.kapok.distributed.common.node;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import com.google.common.collect.ImmutableMap;

public interface WorkerManager {
    ImmutableMap<String, WorkerInfo> getWorkers();
}
