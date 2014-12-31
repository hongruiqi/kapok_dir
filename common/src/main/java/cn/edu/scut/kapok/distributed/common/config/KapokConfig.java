package cn.edu.scut.kapok.distributed.common.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static com.google.common.base.Preconditions.*;

public class KapokConfig {

    private static final Logger logger = LoggerFactory.getLogger(KapokConfig.class);
    private static final String CONFIG_PATH = "kapok.json";

    // config is cached when first parsed.
    private static Supplier<JsonObject> supplier = Suppliers.memoize(new Supplier<JsonObject>() {
        @Override
        public JsonObject get() {
            return loadConfig();
        }
    });

    private static JsonObject loadConfig() {
        InputStream stream = KapokConfig.class.getClassLoader().getResourceAsStream(CONFIG_PATH);
        checkNotNull(stream, "kapok.json can't be found in classpath or can't be opened");
        try {
            Reader reader = new InputStreamReader(stream);
            Gson gson = new Gson();
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            logger.error("illegal config file format", e);
            throw new RuntimeException(e);
        } finally {
            Closeables.closeQuietly(stream);
        }
    }

    public static JsonObject get() {
        return supplier.get();
    }
}
