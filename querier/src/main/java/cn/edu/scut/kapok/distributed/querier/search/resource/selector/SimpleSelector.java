package cn.edu.scut.kapok.distributed.querier.search.resource.selector;

import cn.edu.scut.kapok.distributed.protos.QueryProto.Query;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class SimpleSelector implements Selector {
    @Override
    public List<WorkerInfo> selectResource(Query query, List<WorkerInfo> resources) throws SelectException {
        return ImmutableList.copyOf(resources);
    }
}
