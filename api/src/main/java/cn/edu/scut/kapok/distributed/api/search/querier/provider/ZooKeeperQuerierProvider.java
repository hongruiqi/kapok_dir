package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.api.search.exception.QuerierNotFoundException;
import cn.edu.scut.kapok.distributed.common.node.impl.zookeeper.ZooKeeperQuerierManager;
import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;

@Singleton
public class ZooKeeperQuerierProvider implements QuerierProvider {

    private final ZooKeeperQuerierManager querierManager;
    private final Random random = new Random();

    @Inject
    public ZooKeeperQuerierProvider(ZooKeeperQuerierManager querierManager) {
        this.querierManager = querierManager;
    }

    @Override
    public QuerierInfo get() throws QuerierNotFoundException {
        ImmutableMap<String, QuerierInfo> queriers = querierManager.getQueriers();
        if (queriers.size() == 0) {
            throw new QuerierNotFoundException();
        }
        int i = random.nextInt(queriers.size());
        return queriers.entrySet().asList().get(i).getValue();
    }
}
