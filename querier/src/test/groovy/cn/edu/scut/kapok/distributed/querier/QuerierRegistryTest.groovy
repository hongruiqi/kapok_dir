package cn.edu.scut.kapok.distributed.querier

import cn.edu.scut.kapok.distributed.protos.QuerierInfo
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class QuerierRegistryTest {

    String addr
    CuratorFramework cf

    @Before
    void setUp() {
        addr = "192.168.1.1"
        cf = mock(CuratorFramework.class)
    }

    @Test
    void testQuerierRegistry() {
        def registry = spy(new QuerierRegistry(addr, cf))
        def node = mock(PersistentEphemeralNode.class)
        doReturn(node).when(registry).createNode(any(QuerierInfo.class))
        registry.start()
        def captor = ArgumentCaptor.forClass(QuerierInfo.class)
        verify(registry).createNode(captor.capture())
        assert captor.value.addr == addr
        verify(node).start()
        registry.close()
        verify(node).close()
    }
}
