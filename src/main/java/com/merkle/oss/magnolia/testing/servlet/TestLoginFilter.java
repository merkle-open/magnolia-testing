package com.merkle.oss.magnolia.testing.servlet;

import info.magnolia.cms.security.auth.login.BasicLogin;
import info.magnolia.cms.security.auth.login.LoginFilter;
import info.magnolia.cms.security.auth.login.LoginHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collection;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.google.inject.Inject;

public class TestLoginFilter extends LoginFilter {

    @Inject
    public TestLoginFilter() {}

    @Override
    public Collection<LoginHandler> getLoginHandlers() {
        return Set.of(new BasicLogin());
    }

    public static class InstallTask extends AbstractRepositoryTask {
        public InstallTask() {
            super("InstallTestLoginFilter", "Installs test login filter");
        }
        @Override
        protected void doExecute(final InstallContext installContext) throws RepositoryException {
            final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
            final Node filter = session.getNode("/server/filters/login");
            filter.setProperty("class", TestLoginFilter.class.getName());
        }
    }
}
