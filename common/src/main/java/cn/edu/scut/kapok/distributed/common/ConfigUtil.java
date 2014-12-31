package cn.edu.scut.kapok.distributed.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import static com.google.common.base.Preconditions.*;

public class ConfigUtil {

    // load a JsonObject config to classOfT.
    // RuntimeException is thrown when Json isn't correctly parsed.
    public static <T> T loadConfig(JsonObject json, Class<T> classOfT) {

        try {
            Gson gson = new Gson();
            return gson.fromJson(
                    checkNotNull(json),
                    checkNotNull(classOfT));
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
