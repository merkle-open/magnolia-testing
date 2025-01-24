package com.merkle.oss.magnolia.testing;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.merkle.oss.magnolia.testing.configuration.MagnoliaIntegrationTestInitializer;
import com.merkle.oss.magnolia.testing.repository.RepositoryUtil;
import com.merkle.oss.magnolia.testing.suite.MagnoliaSuiteTestEngine;

public class MagnoliaIntegrationBeforeAllTestExtension implements BeforeAllCallback, AfterAllCallback {
	private final MagnoliaIntegrationTestInitializer magnoliaIntegrationTestInitializer = new MagnoliaIntegrationTestInitializer();

	@Override
	public void beforeAll(final ExtensionContext testContext) throws Exception {
		final Context.TestContextWrapper context = new Context.TestContextWrapper(testContext);
		if(!MagnoliaSuiteTestEngine.isRunningInMagnoliaTestSuite(testContext)) {
			magnoliaIntegrationTestInitializer.init(context);
			magnoliaIntegrationTestInitializer.start(true);
		}
		new RepositoryUtil().load(context);
	}

	@Override
	public void afterAll(final ExtensionContext testContext) {
		if(!MagnoliaSuiteTestEngine.isRunningInMagnoliaTestSuite(testContext)) {
			magnoliaIntegrationTestInitializer.stop();
			magnoliaIntegrationTestInitializer.destroy();
		}
	}
}