package cn.edu.scut.kapok.distributed.worker
import cn.edu.scut.kapok.distributed.common.ZKPath
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import static cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo
import static org.mockito.Mockito.*

class WorkerRegistryTest {

    CuratorFramework cf
    PersistentEphemeralNode node
    WorkerRegistry registry
    ArgumentCaptor<WorkerInfo> workerInfoCaptor
    ArgumentCaptor<PersistentEphemeralNode.Mode> modeCaptor
    ArgumentCaptor<String> pathCaptor

    @Before
    void setUp() {
        cf = mock(CuratorFramework.class)
        node = mock(PersistentEphemeralNode.class)
        registry = spy(new WorkerRegistry(
                "TestWorker",
                "UUID",
                "192.168.0.1",
                cf
        ))
        workerInfoCaptor = ArgumentCaptor.forClass(WorkerInfo.class)
        modeCaptor = ArgumentCaptor.forClass(PersistentEphemeralNode.Mode.class)
        pathCaptor = ArgumentCaptor.forClass(String.class)
        doReturn(node).when(registry).createNode(
                modeCaptor.capture(),
                pathCaptor.capture(),
                workerInfoCaptor.capture())
    }

    @Test
    void testRegistry() {
        registry.start()
        registry.close()
        verify(node).start()
        verify(node).close()

        def workerInfo = workerInfoCaptor.value
        assert workerInfo.getName() == "TestWorker"
        assert workerInfo.getUuid() == "UUID"
        assert workerInfo.getAddr() == "192.168.0.1"
        assert modeCaptor.value == PersistentEphemeralNode.Mode.PROTECTED_EPHEMERAL_SEQUENTIAL
        assert pathCaptor.value == ZKPath.WORKERS + "/instance-"
    }
}
