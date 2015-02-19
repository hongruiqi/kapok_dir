package cn.edu.scut.kapok.distributed.common;

import groovy.lang.GroovyClassLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class ModuleServiceUtil {
    public static ModuleService load(String name) throws Exception {
        @SuppressWarnings("unchecked")
        GroovyClassLoader loader = (GroovyClassLoader) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return new GroovyClassLoader();
                    }
                }
        );
        Class cls = loader.loadClass(name);
        return (ModuleService) cls.newInstance();
    }
}
