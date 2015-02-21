package cn.edu.scut.kapok.distributed.common.node;

import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
import com.google.common.collect.ImmutableMap;

public interface WorkerMonitor {
    ImmutableMap<String, WorkerInfo> getWorkers();
}
