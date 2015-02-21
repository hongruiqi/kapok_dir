package cn.edu.scut.kapok.distributed.common.node;

import cn.edu.scut.kapok.distributed.protos.QuerierInfo;
import com.google.common.collect.ImmutableMap;

public interface QuerierMonitor {
    ImmutableMap<String, QuerierInfo> getQueriers();
}
