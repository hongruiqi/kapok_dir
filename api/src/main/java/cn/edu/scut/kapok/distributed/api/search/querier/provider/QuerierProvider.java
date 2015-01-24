package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.api.search.exception.QuerierNotFoundException;
import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;

public interface QuerierProvider {
    QuerierInfo get() throws QuerierNotFoundException;
}
