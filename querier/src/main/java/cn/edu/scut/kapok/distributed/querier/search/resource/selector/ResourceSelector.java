package cn.edu.scut.kapok.distributed.querier.search.resource.selector;

import cn.edu.scut.kapok.distributed.protos.QueryProto.Query;

import java.util.Collection;

public interface ResourceSelector {
    Collection<String> selectResource(Query query, Collection<String> resources);
}
