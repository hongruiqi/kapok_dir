package cn.edu.scut.kapok.distributed.worker

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode

import static cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo
import static org.mockito.Matchers.any
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify

class WorkerRegistryTest extends GroovyTestCase {

    CuratorFramework cf
    PersistentEphemeralNode node
    WorkerRegistry registry

    void setUp() {
        cf = mock(CuratorFramework.class)
        node = mock(PersistentEphemeralNode.class)
        registry = spy(new WorkerRegistry(
                "TestWorker",
                "UUID",
                "192.168.0.0.1",
                cf
        ))
        doReturn(node).when(registry).createNode(any(CuratorFramework.class), any(WorkerInfo.class))
    }

    void testRegistry() {
        registry.start()
        registry.close()
        verify(node).start()
        verify(node).close()
    }
}
