package com.merkle.oss.magnolia.testing;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.merkle.oss.magnolia.testing.configuration.MagnoliaIntegrationTestInitializer;
import com.merkle.oss.magnolia.testing.repository.RepositoryUtil;

public class MagnoliaGuiceContextTestExtension implements BeforeEachCallback, AfterEachCallback {
	private final MagnoliaIntegrationTestInitializer magnoliaIntegrationTestInitializer = new MagnoliaIntegrationTestInitializer();

	@Override
	public void beforeEach(final ExtensionContext testContext) throws Exception {
		final Context.TestContextWrapper context = new Context.TestContextWrapper(testContext);
		magnoliaIntegrationTestInitializer.init(context);
		new RepositoryUtil().load(context);
	}

	@Override
	public void afterEach(final ExtensionContext testContext) {
		magnoliaIntegrationTestInitializer.destroy();
	}
}