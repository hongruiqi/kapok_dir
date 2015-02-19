package cn.edu.scut.kapok.distributed.common.node.impl.zk
import cn.edu.scut.kapok.distributed.common.ProtoParser
import com.google.protobuf.InvalidProtocolBufferException
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.listen.ListenerContainer
import org.apache.curator.framework.recipes.cache.ChildData
import org.apache.curator.framework.recipes.cache.PathChildrenCache
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class NodeManagerTest {

    CuratorFramework cf
    ProtoParser parser
    NodeEventListener listener
    PathChildrenCache cache
    String path
    ListenerContainer container
    PathChildrenCacheEvent event
    NodeManager manager
    ChildData childData
    Object dummy = new Object()

    @Before
    void setUp() {
        cf = mock(CuratorFramework.class)
        parser = mock(ProtoParser.class)
        listener = mock(NodeEventListener.class)
        cache = mock(PathChildrenCache.class)
        path = "/test"
        container = mock(ListenerContainer.class)
        event = mock(PathChildrenCacheEvent.class)
        manager = spy(new NodeManager(path, parser, listener, cf))
        childData = mock(ChildData.class)
    }

    private void startManager() {
        doReturn(cache).when(manager).createPathChildrenCache(cf, path, true)
        when(cache.getListenable()).thenReturn(container)
        doNothing().when(container).addListener(any(PathChildrenCacheListener.class))
        manager.start()
    }

    @Test
    void testNodeManagerStartStop() {
        startManager()
        verify(cache).start()
        verify(container).addListener(any(PathChildrenCacheListener.class))
        manager.close()
        verify(cache).close()
    }

    private void doChildChangeTest(boolean parseError=false, Closure c) {
        startManager()
        if (!parseError) {
            when(parser.parseFrom(any(byte[].class))).thenReturn(dummy)
        } else {
            when(parser.parseFrom(any(byte[].class))).thenThrow(InvalidProtocolBufferException.class)
        }
        def captor = ArgumentCaptor.forClass(PathChildrenCacheListener.class)
        verify(container).addListener(captor.capture())
        def cacheListener = captor.value
        when(event.getData()).thenReturn(childData)
        def data = new byte[1]
        when(childData.getData()).thenReturn(data)
        c(cacheListener, data)
    }

    @Test
    void testNodeManagerAddChild() {
        doChildChangeTest { cacheListener, data ->
            when(event.getType()).thenReturn(PathChildrenCacheEvent.Type.CHILD_ADDED)
            cacheListener.childEvent(cf, event)
            verify(parser).parseFrom(data)
            verify(listener).onAdd(dummy)
            verifyNoMoreInteractions(listener)
        }
    }

    @Test
    void testNodeManagerUpdateChild() {
        doChildChangeTest { cacheListener, data ->
            when(event.getType()).thenReturn(PathChildrenCacheEvent.Type.CHILD_UPDATED)
            cacheListener.childEvent(cf, event)
            verify(parser).parseFrom(data)
            verify(listener).onUpdate(dummy)
            verifyNoMoreInteractions(listener)
        }
    }

    @Test
    void testNodeManagerRemoveChild() {
        doChildChangeTest { cacheListener, data ->
            when(event.getType()).thenReturn(PathChildrenCacheEvent.Type.CHILD_REMOVED)
            cacheListener.childEvent(cf, event)
            verify(parser).parseFrom(data)
            verify(listener).onRemove(dummy)
            verifyNoMoreInteractions(listener)
        }
    }

    @Test
    void testDataParseError() {
        doChildChangeTest(true) { cacheListener, data ->
            when(event.getType()).thenReturn(PathChildrenCacheEvent.Type.CHILD_ADDED)
            cacheListener.childEvent(cf, event)
            verify(parser).parseFrom(data)
            verifyZeroInteractions(listener)
        }
    }

    @Test
    void testEventTypeIgnore() {
        doChildChangeTest { cacheListener, data ->
            when(event.getType()).thenReturn(PathChildrenCacheEvent.Type.INITIALIZED)
            cacheListener.childEvent(cf, event)
            verifyZeroInteractions(listener)
        }
    }
}
