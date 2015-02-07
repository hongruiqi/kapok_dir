package cn.edu.scut.kapok.distributed.worker.servlet;

import cn.edu.scut.kapok.distributed.protos.WorkerInfoProto.WorkerInfo;

import javax.inject.Inject;
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
    public InfoServlet(String workerName,
                       String workerUUID,
                       String workerAddr) {
        this.workerName = workerName;
        this.workerUUID = workerUUID;
        this.workerAddr = workerAddr;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Build protobuf message.
        final WorkerInfo info = WorkerInfo.newBuilder().setName(workerName)
                .setUuid(workerUUID)
                .setAddr(workerAddr).build();
        try (OutputStream out = resp.getOutputStream()) {
            info.writeTo(out);
        }
    }
}
