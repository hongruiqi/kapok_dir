package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.common.CommonModule;
import cn.edu.scut.kapok.distributed.common.node.WorkerManager;
import cn.edu.scut.kapok.distributed.querier.servlet.ServletsConfigModule;
import com.google.inject.*;
import com.google.inject.name.Names;
import groovy.lang.GroovyClassLoader;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkState;

// Querier is the start point of the querier.
public final class QuerierMain {

    private static final Logger logger = LoggerFactory.getLogger(QuerierMain.class);

    // Hooks that is called when shutdown.
    private static Stack<Runnable> shutdownHooks = new Stack<>();

    // Register shutdown hook thread.
    private static void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                while (!shutdownHooks.empty()) {
                    Runnable hook = shutdownHooks.pop();
                    hook.run();
                }
            }
        });
    }

    // Start CuratorFramework and add shutdown hook.
    private static void setupCuratorFramework(Injector injector) {
        checkState(Scopes.isSingleton(injector.getBinding(CuratorFramework.class)),
                "CuratorFramework must be bound as singleton.");
        final CuratorFramework cf = injector.getInstance(CuratorFramework.class);
        cf.start();
        shutdownHooks.push(new Runnable() {
            @Override
            public void run() {
                cf.close();
                logger.info("CuratorFramework closed");
            }
        });
    }

    // Start Registry and add shutdown hook.
    private static void setupRegistry(Injector injector) {
        checkState(Scopes.isSingleton(injector.getBinding(QuerierRegistry.class)),
                "QuerierRegistry must be bound as singleton.");
        final QuerierRegistry registry = injector.getInstance(QuerierRegistry.class);
        registry.start();
        logger.info("querier registry start");
        shutdownHooks.push(new Runnable() {
            @Override
            public void run() {
                try {
                    registry.close();
                    logger.info("registry closed");
                } catch (IOException e) {
                    logger.error("registry close error", e);
                    // ignore intended.
                }
            }
        });
    }

    private static void setupWorkerManager(Injector injector) {
        checkState(Scopes.isSingleton(injector.getBinding(WorkerManager.class)),
                "WorkerManager must be bound as singleton.");
        final WorkerManager workerManager = injector.getInstance(WorkerManager.class);
        try {
            workerManager.start();
            logger.info("worker manager start");
        } catch (Exception e) {
            logger.error("worker manager can' start", e);
            return;
        }
        shutdownHooks.push(new Runnable() {
            @Override
            public void run() {
                try {
                    workerManager.close();
                    logger.info("worker manager closed");
                } catch (Exception e) {
                    logger.error("workerManager close error", e);
                    // ignore intended.
                }
            }
        });
    }

    // Start querier server.
    private static void startQuerierServer(Injector injector) {
        // start server.
        checkState(Scopes.isSingleton(injector.getBinding(QuerierServer.class)),
                "QuerierServer must be bound as singleton.");
        QuerierServer querierServer = injector.getInstance(QuerierServer.class);
        try {
            String serverAddr = injector.getInstance(Key.get(String.class, Names.named("querier.Addr")));
            logger.info("server listening at: {}", serverAddr);
            querierServer.start();
        } catch (Exception e) {
            logger.error("can't start querierServer", e);
        }
    }

    private static Module loadConfigModule() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        GroovyClassLoader loader = new GroovyClassLoader();
        return (Module)loader.loadClass("QuerierModule").newInstance();
    }

    public static void main(String[] args) throws Exception {
        // create injector.
        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                new CommonModule(),
                loadConfigModule(),
                new ServletsConfigModule());

        // init components.
        initShutdownHook();
        setupCuratorFramework(injector);
        setupWorkerManager(injector);
        setupRegistry(injector);

        // start server.
        startQuerierServer(injector);
    }
}
