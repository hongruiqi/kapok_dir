package cn.edu.scut.kapok.distributed.querier.api.search.resource.selector;

import cn.edu.scut.kapok.distributed.protos.Query;
import cn.edu.scut.kapok.distributed.protos.WorkerInfo;

import java.util.List;

public interface Selector {
    List<WorkerInfo> selectResource(Query query, List<WorkerInfo> resources) throws SelectException;
}
