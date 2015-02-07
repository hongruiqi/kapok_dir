package cn.edu.scut.kapok.distributed.worker.servlet;

import com.google.inject.servlet.ServletModule;

public class ServletsConfigModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/search").with(SearchServlet.class);
        serve("/info").with(InfoServlet.class);
    }
}
