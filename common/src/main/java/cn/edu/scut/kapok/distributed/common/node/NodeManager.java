package cn.edu.scut.kapok.distributed.common.node;

import cn.edu.scut.kapok.distributed.common.ProtoParser;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.*;

public class NodeManager<E> {

    private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);

    private final String path;
    private final ProtoParser<E> protoParser;
    private final CuratorFramework cf;
    private final NodeEventListener<E> listener;
    private PathChildrenCache cache;

    public NodeManager(String path, ProtoParser<E> protoParser, NodeEventListener<E> listener, CuratorFramework cf) {
        this.path = checkNotNull(path);
        this.protoParser = checkNotNull(protoParser);
        this.listener = checkNotNull(listener);
        this.cf = checkNotNull(cf);
    }

    public void start() throws Exception {
        cache = new PathChildrenCache(cf, path, true);
        setChangeListener();
        cache.start();
    }

    public void close() throws IOException {
        cache.close();
    }

    // listen on node change.
    private void setChangeListener() {
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                E info = null;
                switch (event.getType()) {
                    case CHILD_ADDED: // fallthrough
                    case CHILD_REMOVED: // fallthrough
                    case CHILD_UPDATED:
                        // data stored in node is presented as protobuf,
                        // so parse it to instance.
                        byte[] data = event.getData().getData();
                        try {
                            info = protoParser.parseFrom(data);
                        } catch (InvalidProtocolBufferException e) {
                            logger.warn("invalid node data: {}", event.getData().getPath());
                            return;
                        }
                        break;
                    default:
                        logger.debug("unhandle event: {}", event.getType());
                        // ignore other events
                }
                // change nodes mapping accroding to event type.
                switch (event.getType()) {
                    case CHILD_ADDED:
                        listener.onAdd(info);
                        break;
                    case CHILD_UPDATED:
                        listener.onUpdate(info);
                        break;
                    case CHILD_REMOVED:
                        listener.onRemove(info);
                        break;
                }
            }
        });
    }
}
