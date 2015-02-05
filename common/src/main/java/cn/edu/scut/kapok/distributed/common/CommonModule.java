package cn.edu.scut.kapok.distributed.common;

import cn.edu.scut.kapok.distributed.common.node.WorkerManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CommonModule extends AbstractModule {
    @Override
    protected void configure() {
        loadProperties();
        bind(WorkerManager.class).in(Singleton.class);
    }

    private void loadProperties() {
        InputStream stream = CommonModule.class.getClassLoader().getResourceAsStream("kapok.properties");
        if (stream == null) {
            addError(new Exception("kapok.properties not found."));
            return;
        }
        try {
            Properties properties = new Properties();
            properties.load(stream);
            Names.bindProperties(binder(), properties);
        } catch (IOException e) {
            addError(e);
        }
    }

    @Provides
    @Singleton
    CuratorFramework provideCuratorFramework(
            @Named("ZooKeeper.ConnectString") String connectString,
            @Named("ZooKeeper.SessionTimeout") int sessionTimeout) {
        return CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 25))
                .build();
    }
}
