package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.api.search.exception.QuerierNotFoundException;
import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;

public class SingleQuerierProvider implements QuerierProvider {

    private QuerierInfo querierInfo;

    public SingleQuerierProvider(QuerierInfo querierInfo) {
        this.querierInfo = querierInfo;
    }

    public QuerierInfo get() throws QuerierNotFoundException {
        return querierInfo;
    }
}
