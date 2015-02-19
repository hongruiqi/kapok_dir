package cn.edu.scut.kapok.distributed.common.node;

import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import com.google.common.collect.ImmutableMap;

public interface QuerierManager {
    ImmutableMap<String, QuerierInfo> getQueriers();
}
