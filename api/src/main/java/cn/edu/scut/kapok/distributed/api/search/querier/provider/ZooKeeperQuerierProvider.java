package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.common.node.QuerierMonitor;
import cn.edu.scut.kapok.distributed.protos.QuerierInfo;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class ZooKeeperQuerierProvider implements QuerierProvider {

    private final QuerierMonitor querierMonitor;
    private final Random random = ThreadLocalRandom.current();

    @Inject
    public ZooKeeperQuerierProvider(QuerierMonitor querierMonitor) {
        this.querierMonitor = querierMonitor;
    }

    @Override
    public QuerierInfo get() throws QuerierNotFoundException {
        ImmutableMap<String, QuerierInfo> queriers = querierMonitor.getQueriers();
        if (queriers.size() == 0) {
            throw new QuerierNotFoundException();
        }
        int i = random.nextInt(queriers.size());
        return queriers.entrySet().asList().get(i).getValue();
    }
}
