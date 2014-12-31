package cn.edu.scut.kapok.distributed.querier.search.resource.selector;

import cn.edu.scut.kapok.distributed.protos.QueryProto;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class DefaultResourceSelector implements ResourceSelector {
    @Override
    public Collection<String> selectResource(QueryProto.Query query, Collection<String> resources) {
        return ImmutableList.copyOf(resources);
    }
}
