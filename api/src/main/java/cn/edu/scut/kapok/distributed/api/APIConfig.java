package cn.edu.scut.kapok.distributed.api;

import cn.edu.scut.kapok.distributed.common.config.KapokConfig;
import com.google.gson.JsonObject;

public class APIConfig {
    public static JsonObject get() {
        return KapokConfig.get().getAsJsonObject("API");
    }
}
