package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.ModuleService;
import cn.edu.scut.kapok.distributed.common.ModuleServiceUtil;
import cn.edu.scut.kapok.distributed.worker.server.WorkerServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerMain {

    private static final Logger logger = LoggerFactory.getLogger(WorkerMain.class);

    // Start worker server.
    private static void startWorkerServer(Injector injector) {
        WorkerServer workerServer = injector.getInstance(WorkerServer.class);
        try {
            String serverAddr = injector.getInstance(
                    Key.get(String.class, Names.named(WorkerPropertyNames.WORKDER_ADDR)));
            logger.info("server listening at: {}", serverAddr);
            workerServer.start();
        } catch (Exception e) {
            logger.error("can't start server", e);
        }
    }

    public static void main(String[] args) throws Exception {
        final ModuleService workerModule = ModuleServiceUtil.load("WorkerModule");

        // create injector.
        final Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                workerModule);

        try {
            workerModule.start(injector);
        } catch (Throwable t) {
            logger.error("Can't start worker module.", t);
            workerModule.stop(injector);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                workerModule.stop(injector);
            }
        });

        // start server.
        startWorkerServer(injector);
    }
}
