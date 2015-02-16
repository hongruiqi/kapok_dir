package cn.edu.scut.kapok.distributed.common.node.spi;

import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto;
import com.google.common.collect.ImmutableMap;

public interface QuerierManager {
    ImmutableMap<String, QuerierInfoProto.QuerierInfo> getQueriers();
}
