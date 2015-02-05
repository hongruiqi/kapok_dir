package cn.edu.scut.kapok.distributed.querier.servlets;

import com.google.inject.servlet.ServletModule;

// ServletsConfigModule binds servlet paths with servlets.
public class ServletsConfigModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/test").with(TestServlet.class);
        serve("/search").with(SearchServlet.class);
    }
}
