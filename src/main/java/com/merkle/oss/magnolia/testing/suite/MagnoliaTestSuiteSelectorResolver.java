package com.merkle.oss.magnolia.testing.suite;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.EngineDiscoveryOrchestrator;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherDiscoveryResult;
import org.junit.platform.launcher.core.ServiceLoaderTestEngineRegistry;

import com.machinezoo.noexception.Exceptions;

class MagnoliaTestSuiteSelectorResolver implements SelectorResolver {
    private final EngineDiscoveryOrchestrator engineDiscoveryOrchestrator = new EngineDiscoveryOrchestrator(new ServiceLoaderTestEngineRegistry().loadTestEngines(), Collections.emptyList());

    @Override
    public Resolution resolve(final ClassSelector selector, final Context context) {
        return Optional
                .of(selector.getJavaClass())
                .flatMap(testClass ->
                        AnnotationUtils.findAnnotation(testClass, MagnoliaTestSuite.class).flatMap(testSuite ->
                                context.addToParent(parent ->
                                        Optional.of(createMagnoliaTestSuiteDescriptor(
                                                testSuite,
                                                testClass,
                                                parent.getUniqueId().append("MagnoliaTestSuite", testClass.getName())
                                        ))
                                )
                        )
                )
                .map(Match::exact)
                .map(Resolution::match)
                .orElseGet(Resolution::unresolved);
    }

    private MagnoliaTestSuiteDescriptor createMagnoliaTestSuiteDescriptor(final MagnoliaTestSuite testSuite, final Class<?> testSuiteClass, final UniqueId uniqueId) {
        final MagnoliaTestSuite.TestClassProvider testClassProvider = Exceptions.wrap().get(() -> testSuite.testClassProvider().getConstructor().newInstance());
        final List<Class<?>> testClasses = testClassProvider.get().stream().filter(this::hasTests).toList();

        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .configurationParameter(JupiterConfiguration.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, Boolean.toString(!testSuite.initializeMagnolia()))
                .configurationParameter(MagnoliaSuiteTestEngine.IS_RUNNING_IN_MAGNOLIA_TEST_SUITE_CONFIGURATION_PROPERTY_NAME, "true")
                .configurationParameter(MagnoliaSuiteTestEngine.IS_MAGNOLIA_TEST_SUITE_INITIALIZE_MAGNOLIA_PROPERTY_NAME, Boolean.toString(testSuite.initializeMagnolia()))
                .selectors(testClasses.stream().map(DiscoverySelectors::selectClass).toList())
                .selectors(DiscoverySelectors.selectUniqueId(uniqueId))
                .build();
        final LauncherDiscoveryResult discoveryResult = engineDiscoveryOrchestrator.discover(request, uniqueId);
        final MagnoliaTestSuiteDescriptor testSuiteDescriptor = new MagnoliaTestSuiteDescriptor(uniqueId, testSuite.failIfNoTests(), testSuite.initializeMagnolia(), testSuiteClass, discoveryResult);

        discoveryResult.getTestEngines()
                .stream()
                .map(discoveryResult::getEngineTestDescriptor)
                .forEach(testSuiteDescriptor::addChild);
        return testSuiteDescriptor;
    }

    private boolean hasTests(final Class<?> clazz) {
        return AnnotationUtils.findAnnotation(clazz, Test.class).isPresent() ||
                !AnnotationUtils.findAnnotatedMethods(clazz, Test.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).isEmpty();
    }
}
