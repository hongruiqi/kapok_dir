package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.protos.QuerierInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SingleQuerierProvider implements QuerierProvider {

    private final QuerierInfo querierInfo;

    @Inject
    public SingleQuerierProvider(QuerierInfo querierInfo) {
        this.querierInfo = querierInfo;
    }

    public QuerierInfo get() throws QuerierNotFoundException {
        return querierInfo;
    }
}
