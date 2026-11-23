package com.merkle.oss.magnolia.testing.configuration;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import java.util.List;

import com.merkle.oss.magnolia.testing.configuration.setup.ConfigureTestUsersTask;

import jakarta.inject.Inject;

public class TestingTestModuleVersionHandler extends DefaultModuleVersionHandler {
	private final ConfigureTestUsersTask configureTestUsersTask;

	@Inject
	public TestingTestModuleVersionHandler(final ConfigureTestUsersTask configureTestUsersTask) {
		this.configureTestUsersTask = configureTestUsersTask;
	}

	@Override
	protected final List<Task> getExtraInstallTasks(final InstallContext installContext) {
		return List.of(configureTestUsersTask);
	}
}
