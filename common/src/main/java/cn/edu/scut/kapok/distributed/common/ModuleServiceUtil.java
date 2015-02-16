package cn.edu.scut.kapok.distributed.common;

import groovy.lang.GroovyClassLoader;

public class ModuleServiceUtil {
    public static ModuleService load(String name) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader();
        Class cls = loader.loadClass(name);
        return (ModuleService) cls.newInstance();
    }
}
