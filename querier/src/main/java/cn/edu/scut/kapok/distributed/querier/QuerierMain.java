package cn.edu.scut.kapok.distributed.querier;

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

// querier is the start point of the querier.
public final class QuerierMain {

    private static final Logger logger = LoggerFactory.getLogger(QuerierMain.class);

    // Start querier server.
    private static void startWorkerServer(Injector injector) {
        HttpServer workerServer = injector.getInstance(HttpServer.class);
        try {
            String querierAddr = injector.getInstance(
                    Key.get(String.class, Names.named("querier.addr")));
            logger.info("server listening at: {}", querierAddr);
            workerServer.start();
            workerServer.join();
        } catch (Exception e) {
            logger.error("can't start server", e);
        }
    }

    public static void main(String[] args) throws Exception {
        final ModuleService querierModule = ModuleServiceUtil.load("QuerierModule");

        // create injector.
        final Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                querierModule);

        try {
            querierModule.start(injector);
        } catch (Throwable t) {
            logger.error("Can't start querier module.", t);
            querierModule.stop(injector);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                querierModule.stop(injector);
            }
        });

        // start server.
        startWorkerServer(injector);
    }
}
