package com.merkle.oss.magnolia.testing.configuration;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import java.util.List;

public class TestModuleVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected final List<Task> getExtraInstallTasks(final InstallContext installContext) {
        return List.of(
        );
    }
}
