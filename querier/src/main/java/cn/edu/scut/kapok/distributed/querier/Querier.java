package cn.edu.scut.kapok.distributed.querier;

import cn.edu.scut.kapok.distributed.common.CuratorFactory;
import cn.edu.scut.kapok.distributed.common.config.KapokConfig;
import cn.edu.scut.kapok.distributed.querier.server.QuerierServer;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.*;

// Querier is the start point of the querier.
// It starts all the services that querier uses.
public class Querier {

    private static final Logger logger = LoggerFactory.getLogger(Querier.class);

    public static void main(String[] args) {
        logger.info("querier starting");

        // parse config
        JsonObject configObj = KapokConfig.get().getAsJsonObject("Querier");
        checkNotNull(configObj, "Querier's config not found");
        QuerierConfig config = new Gson().fromJson(configObj, QuerierConfig.class);
        logger.info("using config: {}", config);

        // Address that others connect to the server.
        String querierAddr = Joiner.on(":").join(
                checkNotNull(config.Server.Host),
                config.Server.Port);

        final CuratorFramework cf = CuratorFactory.getInstance();
        cf.start();

        final QuerierProvider provider = new QuerierProvider(querierAddr, cf);
        provider.start();

        final QuerierServer querierServer = new QuerierServer(config.Server.Host, config.Server.Port, cf);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    provider.close();
                } catch (Exception e) {
                    logger.error("stop provider", e);
                    // ignored intended.
                }
                cf.close();
                try {
                    querierServer.stop();
                } catch (Exception e) {
                    logger.error("stop querier server", e);
                    // ignored intended.
                }
            }
        });

        try {
            logger.info("start server");
            querierServer.start();
        } catch (Exception e) {
            logger.error("can't start querierServer", e);
            try {
                provider.close();
            } catch (IOException e1) {
                logger.error("stop provider", e1);
                // ignored intended.
            }
            cf.close();
            try {
                querierServer.stop();
            } catch (Exception e1) {
                logger.error("stop querier server", e1);
                // ignored intended.
            }
        }
    }
}
