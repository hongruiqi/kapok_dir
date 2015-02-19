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

@Singleton
public class InfoServlet extends HttpServlet {

    private final String workerName;
    private final String workerUUID;
    private final String workerAddr;

    @Inject
    public InfoServlet(@Named(WorkerPropertyNames.WORKDER_NAME) String workerName,
                       @Named(WorkerPropertyNames.WORKDER_UUID) String workerUUID,
                       @Named(WorkerPropertyNames.WORKDER_ADDR) String workerAddr) {
        this.workerName = workerName;
        this.workerUUID = workerUUID;
        this.workerAddr = workerAddr;
    }

    WorkerInfo buildWorkerInfo() {
        return WorkerInfo.newBuilder().setName(workerName)
                .setUuid(workerUUID)
                .setAddr(workerAddr).build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (OutputStream out = resp.getOutputStream()) {
            buildWorkerInfo().writeTo(out);
        }
    }
}
