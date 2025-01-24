package com.merkle.oss.magnolia.testing;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.platform.commons.support.ReflectionSupport;

import com.merkle.oss.magnolia.testing.suite.MagnoliaTestSuite;

@MagnoliaTestSuite(testClassProvider = IntegrationTestSuite.TestClassProvider.class)
public class IntegrationTestSuite {
	public static class TestClassProvider implements MagnoliaTestSuite.TestClassProvider {
		private static final Set<Class<? extends Extension>> INTEGRATION_TEST_EXTENSION_CLASSES = Set.of(MagnoliaIntegrationBeforeAllTestExtension.class, MagnoliaIntegrationTestExtension.class);

		@Override
		public List<Class<?>> get() {
			return ReflectionSupport.findAllClassesInPackage(
				"com.merkle.oss.magnolia.testing",
				this::annotatedWithMagnoliaIntegrationTestExtension,
				ignored -> true
			);
		}

		private boolean annotatedWithMagnoliaIntegrationTestExtension(final Class<?> clazz) {
			return Optional
					.ofNullable(clazz.getAnnotation(ExtendWith.class))
					.map(ExtendWith::value)
					.stream()
					.flatMap(Arrays::stream)
					.anyMatch(INTEGRATION_TEST_EXTENSION_CLASSES::contains);
		}
	}
}