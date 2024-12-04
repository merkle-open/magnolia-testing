package com.merkle.oss.magnolia.testing.configuration;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import java.util.List;

import com.merkle.oss.magnolia.testing.servlet.DeleteFilterTask;
import com.merkle.oss.magnolia.testing.servlet.TestContextFilter;
import com.merkle.oss.magnolia.testing.servlet.TestLoginFilter;

public class TestModuleVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected final List<Task> getExtraInstallTasks(final InstallContext installContext) {
        return List.of(
                new TestContextFilter.InstallTask(),
                new TestLoginFilter.InstallTask(),
                new DeleteFilterTask("logout"),
                new DeleteFilterTask("securityCallback")
        );
    }
}
