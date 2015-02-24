package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.protos.QuerierInfo;

public interface QuerierProvider {
    QuerierInfo get() throws QuerierNotFoundException;
}
