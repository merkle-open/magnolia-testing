package com.merkle.oss.magnolia.testing;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.merkle.oss.magnolia.testing.configuration.MagnoliaIntegrationTestInitializer;
import com.merkle.oss.magnolia.testing.repository.RepositoryUtil;

public class MagnoliaGuiceContextTestExtension implements BeforeEachCallback, AfterEachCallback {
	private final MagnoliaIntegrationTestInitializer magnoliaIntegrationTestInitializer = new MagnoliaIntegrationTestInitializer();

	@Override
	public void beforeEach(final ExtensionContext context) throws Exception {
		magnoliaIntegrationTestInitializer.init(context);
		new RepositoryUtil().load(context.getRequiredTestMethod());
	}

	@Override
	public void afterEach(final ExtensionContext context) {
		magnoliaIntegrationTestInitializer.destroy();
	}
}