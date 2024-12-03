package com.merkle.oss.magnolia.testing.module;

import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.delta.Condition;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.setup.CoreModuleVersionHandler;

import java.util.List;

import com.google.inject.Inject;

public class TestCoreModuleVersionHandler extends CoreModuleVersionHandler {
	@Inject
	public TestCoreModuleVersionHandler(
		final RepositoryManager repositoryManager,
		final NodeNameHelper nodeNameHelper
	) {
		super(repositoryManager, nodeNameHelper);
	}

	@Override
	protected List<Condition> getInstallConditions() {
		return List.of();
	}
}
