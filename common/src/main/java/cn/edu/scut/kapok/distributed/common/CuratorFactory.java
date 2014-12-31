package cn.edu.scut.kapok.distributed.common;

import cn.edu.scut.kapok.distributed.common.config.KapokConfig;
import cn.edu.scut.kapok.distributed.common.config.ZooKeeperConfig;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import static com.google.common.base.Preconditions.*;

// Factory to create CuratorFramework instance.
public class CuratorFactory {

    // Create an instance of CuratorFramework using config.
    public static CuratorFramework getInstance() {
        JsonObject configObj = KapokConfig.get().getAsJsonObject("Common").getAsJsonObject("ZooKeeper");
        checkNotNull(configObj);
        ZooKeeperConfig zkConfig = new Gson().fromJson(configObj, ZooKeeperConfig.class);
        String connectString = Joiner.on(",").join(zkConfig.Addrs);
        return CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(zkConfig.SessionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 25))
                .build();
    }
}
