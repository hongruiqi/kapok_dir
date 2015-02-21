package cn.edu.scut.kapok.distributed.querier.search.impl.resource.selector;

import cn.edu.scut.kapok.distributed.protos.Query;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.selector.SelectException;
import cn.edu.scut.kapok.distributed.querier.api.search.resource.selector.Selector;
import com.google.common.collect.ImmutableList;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SimpleSelector implements Selector {
    @Override
    public List<WorkerInfo> selectResource(Query query, List<WorkerInfo> resources) throws SelectException {
        return ImmutableList.copyOf(resources);
    }
}
