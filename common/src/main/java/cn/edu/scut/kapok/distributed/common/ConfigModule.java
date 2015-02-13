package cn.edu.scut.kapok.distributed.common;

import com.google.inject.servlet.ServletModule;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfigModule extends ServletModule {

    final String filename;

    public ConfigModule(String filename) {
        this.filename = filename;
    }

    @Override
    protected void configureServlets() {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setScriptBaseClass(DelegatingScript.class.getName());
        GroovyShell shell = new GroovyShell(cc);
        DelegatingScript script;
        try {
            URL absPath = ConfigModule.class.getClassLoader().getResource(filename);
            checkNotNull(absPath, "%s not found in classpath", filename);
            script = (DelegatingScript) shell.parse(
                    absPath.toURI());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        script.setDelegate(this);
        script.run();
    }
}
