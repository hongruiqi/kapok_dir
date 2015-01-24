package cn.edu.scut.kapok.distributed.api.search.querier.provider;

import cn.edu.scut.kapok.distributed.api.search.exception.QuerierNotFoundException;
import cn.edu.scut.kapok.distributed.common.node.QuerierManager;
import cn.edu.scut.kapok.distributed.protos.QuerierInfoProto.QuerierInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;
import java.util.Random;

public class ZooKeeperQuerierProvider implements QuerierProvider {

    private final QuerierManager querierManager;
    private final Random random = new Random();

    public ZooKeeperQuerierProvider(CuratorFramework cf) {
        querierManager = new QuerierManager(cf);
    }

    public void start() throws Exception {
        querierManager.start();
    }

    public void close() throws IOException {
        querierManager.close();
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
