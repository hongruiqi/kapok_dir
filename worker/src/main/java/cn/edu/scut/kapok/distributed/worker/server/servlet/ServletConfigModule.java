package cn.edu.scut.kapok.distributed.worker.server.servlet;

import com.google.inject.servlet.ServletModule;

public class ServletConfigModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/search").with(SearchServlet.class);
    }
}
