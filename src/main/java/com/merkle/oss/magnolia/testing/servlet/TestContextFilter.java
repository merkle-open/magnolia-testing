package com.merkle.oss.magnolia.testing.servlet;

import info.magnolia.cms.filters.ContextFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.context.WebContextFactory;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

public class TestContextFilter extends ContextFilter {
    private final WebContextFactory webContextFactory;
    private ServletContext servletContext;

    @Inject
    public TestContextFilter(final WebContextFactory webContextFactory) {
        super(webContextFactory);
        this.webContextFactory = webContextFactory;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        this.servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        // This filter can be invoked multiple times. The first time it's called it initializes the MgnlContext. On
        // subsequent invocations it only pushes the request and response objects on the stack in WebContext. The
        // multiple invocations result from either dispatching a forward, an include or an error request, and also
        // when the filter chain is reset (which happens when logging out).
        if (!MgnlContext.hasInstance() || MgnlContext.isSystemInstance()) {
            final WebContext webContext = webContextFactory.createWebContext(request, response, servletContext);
            MgnlContext.setInstance(webContext);
            webContext.push(request, response);
        }
        chain.doFilter(request, response);
    }

    public static class InstallTask extends AbstractRepositoryTask {
        public InstallTask() {
            super("InstallTestContextFilters", "Installs test context filter");
        }
        @Override
        protected void doExecute(final InstallContext installContext) throws RepositoryException {
            final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
            final Node filter = session.getNode("/server/filters/context");
            filter.setProperty("class", TestContextFilter.class.getName());
        }
    }
}
