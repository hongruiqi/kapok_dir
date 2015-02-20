package cn.edu.scut.kapok.distributed.worker.servlet;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;
import cn.edu.scut.kapok.distributed.worker.WorkerPropertyNames;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * InfoServlet serves information about the worker.
 */
@Singleton
public class InfoServlet extends HttpServlet {

    private final String workerName;
    private final String workerUUID;
    private final String workerAddr;
    private final String workerPath;
    private final byte[] workerInfoBytes;

    /**
     * Create {@code InfoServlet} instance.
     *
     * @param workerName Name of the worker.
     * @param workerUuid Uuid of the worker.
     * @param workerAddr Addr of the worker.
     */
    @Inject
    public InfoServlet(@Named(WorkerPropertyNames.WORKER_NAME) String workerName,
                       @Named(WorkerPropertyNames.WORKER_UUID) String workerUuid,
                       @Named(WorkerPropertyNames.WORKER_ADDR) String workerAddr,
                       @Named(WorkerPropertyNames.WORKER_PATH) String workerPath) {
        this.workerName = workerName;
        this.workerUUID = workerUuid;
        this.workerAddr = workerAddr;
        this.workerPath = workerPath;
        this.workerInfoBytes = buildWorkerInfo().toByteArray();
    }

    private WorkerInfo buildWorkerInfo() {
        return WorkerInfo.newBuilder()
                .setName(workerName)
                .setUuid(workerUUID)
                .setAddr(String.format("http://%s%s", workerAddr, workerPath))
                .build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (OutputStream out = resp.getOutputStream()) {
            out.write(workerInfoBytes);
        }
    }
}
