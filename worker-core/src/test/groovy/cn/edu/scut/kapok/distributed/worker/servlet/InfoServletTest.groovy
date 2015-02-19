package cn.edu.scut.kapok.distributed.worker.servlet

import cn.edu.scut.kapok.distributed.test.ByteArrayServletOutputStream
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class InfoServletTest {

    @Test
    void testInfoServlet() {
        def servlet = new InfoServlet("testName", "testUUID", "192.168.1.1")
        def request = mock(HttpServletRequest.class)
        def response = mock(HttpServletResponse.class)
        def output = new ByteArrayServletOutputStream()
        when(response.getOutputStream()).thenReturn(output)
        servlet.doGet(request, response)
        def expected = WorkerInfo.newBuilder().setName("testName").setUuid("testUUID").setAddr("192.168.1.1").build();
        assert output.toByteArray() == expected.toByteArray()
    }
}
