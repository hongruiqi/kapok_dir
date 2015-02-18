package cn.edu.scut.kapok.distributed.common;

import com.google.inject.Injector;
import com.google.inject.Module;

public interface ModuleService extends Module {
    void start(Injector injector);

    void stop(Injector injector);
}
