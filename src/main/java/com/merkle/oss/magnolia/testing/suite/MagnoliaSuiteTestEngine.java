package com.merkle.oss.magnolia.testing.suite;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

public class MagnoliaSuiteTestEngine implements TestEngine {
	static final String IS_RUNNING_IN_MAGNOLIA_TEST_SUITE_CONFIGURATION_PROPERTY_NAME = "isRunningInMagnoliaTestSuite";
	final EngineDiscoveryRequestResolver<Descriptor> resolver = EngineDiscoveryRequestResolver.<MagnoliaSuiteTestEngine.Descriptor>builder()
			.addClassContainerSelectorResolver(testClass -> findAnnotation(testClass, MagnoliaTestSuite.class).isPresent())
			.addSelectorResolver(context -> new MagnoliaTestSuiteSelectorResolver())
			.build();

	@Override
	public TestDescriptor discover(final EngineDiscoveryRequest discoveryRequest, final UniqueId uniqueId) {
		final Descriptor descriptor = new Descriptor(uniqueId);
		resolver.resolve(discoveryRequest, descriptor);
		descriptor.accept(TestDescriptor::prune);
		return descriptor;
	}

	@Override
	public void execute(final ExecutionRequest request) {
		final Descriptor descriptor = (Descriptor) request.getRootTestDescriptor();
		final EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
		descriptor.getChildren()
				.stream()
				.map(MagnoliaTestSuiteDescriptor.class::cast)
				.forEach(suiteTestDescriptor -> suiteTestDescriptor.execute(engineExecutionListener));
	}

	@Override
	public String getId() {
		return Descriptor.ID;
	}

	public static boolean isRunningInMagnoliaTestSuite(final ExtensionContext context) {
		return context.getConfigurationParameter(MagnoliaSuiteTestEngine.IS_RUNNING_IN_MAGNOLIA_TEST_SUITE_CONFIGURATION_PROPERTY_NAME)
				.map("true"::equals)
				.orElse(false);
	}

	public static class Descriptor extends EngineDescriptor {
		public static final String ID = "IntegrationSuiteTestEngine";
		private Descriptor(final UniqueId uniqueId) {
			super(uniqueId, ID);
		}
	}
}
