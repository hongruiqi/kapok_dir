package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.HttpServer;
import cn.edu.scut.kapok.distributed.common.ModuleService;
import cn.edu.scut.kapok.distributed.common.ModuleServiceUtil;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerMain {

    private static final Logger logger = LoggerFactory.getLogger(WorkerMain.class);

    /**
     * Start the worker server.
     *
     * @param injector Injector is used to inject and get a workerServer instance.
     */
    private static void startWorkerServer(Injector injector) {
        HttpServer workerServer = injector.getInstance(HttpServer.class);
        try {
            String serverAddr = injector.getInstance(
                    Key.get(String.class, Names.named(WorkerPropertyNames.WORKER_ADDR)));
            logger.info("server listening at: {}", serverAddr);
            workerServer.start();
            workerServer.join();
        } catch (Exception e) {
            logger.error("can't start server", e);
        }
    }

    /**
     * Main entry of the worker.
     * <ul>
     * <li>1. Load worker module.</li>
     * <li>2. Create Injector with worker module.</li>
     * <li>3. Start worker module.</li>
     * <li>4. Start worker server.</li>
     * </ul>
     *
     * @param args Args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Load worker module.
        final ModuleService workerModule = ModuleServiceUtil.load("WorkerModule");

        // Create injector.
        final Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                workerModule);

        // Start worker module.
        try {
            workerModule.start(injector);
        } catch (Throwable t) {
            logger.error("Can't start worker module.", t);
            workerModule.stop(injector);
            return;
        }

        // Register shutdown hooks.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                workerModule.stop(injector);
            }
        });

        // Start worker server.
        startWorkerServer(injector);
    }
}
