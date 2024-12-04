package com.merkle.oss.magnolia.testing;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.merkle.oss.magnolia.testing.configuration.MagnoliaIntegrationTestInitializer;
import com.merkle.oss.magnolia.testing.repository.RepositoryUtil;

public class MagnoliaGuiceContextBeforeAllTestExtension implements BeforeAllCallback, AfterAllCallback {
	private final MagnoliaIntegrationTestInitializer magnoliaIntegrationTestInitializer = new MagnoliaIntegrationTestInitializer();

	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		magnoliaIntegrationTestInitializer.init(context);
		new RepositoryUtil().load(context);
	}

	@Override
	public void afterAll(final ExtensionContext context) {
		magnoliaIntegrationTestInitializer.destroy();
	}
}