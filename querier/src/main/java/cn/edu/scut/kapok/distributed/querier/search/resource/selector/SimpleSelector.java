package cn.edu.scut.kapok.distributed.querier.search.resource.selector;

import cn.edu.scut.kapok.distributed.protos.QueryProto;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import com.google.common.collect.ImmutableList;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SimpleSelector implements Selector {
    @Override
    public List<WorkerInfo> selectResource(QueryProto.Query query, List<WorkerInfo> resources) throws SelectException {
        return ImmutableList.copyOf(resources);
    }
}
