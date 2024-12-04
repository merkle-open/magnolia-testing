package com.merkle.oss.magnolia.testing.servlet;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class DeleteFilterTask extends AbstractRepositoryTask {
    private final String name;

    public DeleteFilterTask(final String name) {
        super("Delete" + name + "Filter", "Deletes " + name + " filter");
        this.name = name;
    }

    @Override
    protected void doExecute(final InstallContext installContext) throws RepositoryException {
        final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
        session.removeItem("/server/filters/"+name);
    }
}
