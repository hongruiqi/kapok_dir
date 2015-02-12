package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.common.CommonModule;
import cn.edu.scut.kapok.distributed.worker.server.WorkerServer;
import cn.edu.scut.kapok.distributed.worker.servlet.ServletsConfigModule;
import com.google.inject.*;
import com.google.inject.name.Names;
import groovy.lang.GroovyClassLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkState;

public class WorkerMain {

    private static final Logger logger = LoggerFactory.getLogger(WorkerMain.class);

    // Hooks that is called when shutdown.
    private static final Stack<Runnable> shutdownHooks = new Stack<>();

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
        cf.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("ZooKeeper state change: {}", newState);
            }
        });
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
        checkState(Scopes.isSingleton(injector.getBinding(WorkerRegistry.class)),
                "WorkerRegistry must be bound as singleton.");
        final WorkerRegistry registry = injector.getInstance(WorkerRegistry.class);
        registry.start();
        logger.info("worker registry start");
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

    // Start worker server.
    private static void startWorkerServer(Injector injector) {
        // start server.
        checkState(Scopes.isSingleton(injector.getBinding(WorkerServer.class)),
                "QuerierServer must be bound as singleton.");
        WorkerServer workerServer = injector.getInstance(WorkerServer.class);
        try {
            String serverAddr = injector.getInstance(Key.get(String.class, Names.named("worker.Addr")));
            logger.info("server listening at: {}", serverAddr);
            workerServer.start();
        } catch (Exception e) {
            logger.error("can't start querierServer", e);
        }
    }

    private static Module loadConfigModule() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        GroovyClassLoader loader = new GroovyClassLoader();
        return (Module)loader.loadClass("WorkerModule").newInstance();
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
        setupRegistry(injector);

        // start server.
        startWorkerServer(injector);
    }
}
