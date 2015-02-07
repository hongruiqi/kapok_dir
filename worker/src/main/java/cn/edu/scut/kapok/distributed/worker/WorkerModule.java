package cn.edu.scut.kapok.distributed.worker;

import cn.edu.scut.kapok.distributed.worker.fetch.BBTFetcher;
import cn.edu.scut.kapok.distributed.worker.fetch.Fetcher;
import com.google.inject.AbstractModule;

public class WorkerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Fetcher.class).to(BBTFetcher.class);
    }
}
