package cn.edu.scut.kapok.distributed.querier.search.resource.selector;

import cn.edu.scut.kapok.distributed.protos.QueryProto.Query;
import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;

import java.util.List;

public interface Selector {
    List<WorkerInfo> selectResource(Query query, List<WorkerInfo> resources) throws SelectException;
}
