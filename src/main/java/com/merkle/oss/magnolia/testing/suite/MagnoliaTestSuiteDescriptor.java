package com.merkle.oss.magnolia.testing.suite;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.launcher.core.EngineExecutionOrchestrator;
import org.junit.platform.launcher.core.LauncherDiscoveryResult;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import com.merkle.oss.magnolia.testing.Context;
import com.merkle.oss.magnolia.testing.configuration.MagnoliaIntegrationTestInitializer;

class MagnoliaTestSuiteDescriptor extends AbstractTestDescriptor implements Node<JupiterEngineExecutionContext> {
    private final MagnoliaIntegrationTestInitializer magnoliaIntegrationTestInitializer = new MagnoliaIntegrationTestInitializer();
    private final EngineExecutionOrchestrator executionOrchestrator = new EngineExecutionOrchestrator();
    private final boolean failIfNoTests;
    private final boolean initializeMagnolia;
    private final Class<?> testSuiteClass;
    private final LauncherDiscoveryResult discoveryResult;

    MagnoliaTestSuiteDescriptor(
            final UniqueId uniqueId,
            final boolean failIfNoTests,
            final boolean initializeMagnolia,
            final Class<?> testSuiteClass,
            final LauncherDiscoveryResult discoveryResult
    ) {
        super(uniqueId, "MagnoliaTestSuite");
        this.failIfNoTests = failIfNoTests;
        this.initializeMagnolia = initializeMagnolia;
        this.testSuiteClass = testSuiteClass;
        this.discoveryResult = discoveryResult;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) throws Exception {
        return Node.super.before(context);
    }

    public void execute(final EngineExecutionListener parentEngineExecutionListener) {
        parentEngineExecutionListener.executionStarted(this);
        final ThrowableCollector throwableCollector = new OpenTest4JAwareThrowableCollector();
        final Context context = new Context() {
            @Override
            public Class<?> getClazz() {
                return testSuiteClass;
            }
            @Override
            public Optional<Method> getMethod() {
                return Optional.empty();
            }
        };
        if(initializeMagnolia) {
            executeMethods(AnnotationUtils.findAnnotatedMethods(testSuiteClass, BeforeMagnoliaStart.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN), throwableCollector);
            throwableCollector.execute(() -> magnoliaIntegrationTestInitializer.init(context));
            throwableCollector.execute(() -> magnoliaIntegrationTestInitializer.start(true));
        }
        executeMethods(AnnotationUtils.findAnnotatedMethods(testSuiteClass, BeforeSuite.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN), throwableCollector);
        final TestExecutionSummary summary = executeTests(parentEngineExecutionListener);
        if(initializeMagnolia) {
            executeMethods(AnnotationUtils.findAnnotatedMethods(testSuiteClass, BeforeMagnoliaStop.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN), throwableCollector);
            throwableCollector.execute(magnoliaIntegrationTestInitializer::stop);
            throwableCollector.execute(magnoliaIntegrationTestInitializer::destroy);
        }
        executeMethods(AnnotationUtils.findAnnotatedMethods(testSuiteClass, AfterSuite.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN), throwableCollector);
        final TestExecutionResult testExecutionResult = computeTestExecutionResult(summary, throwableCollector);
        parentEngineExecutionListener.executionFinished(this, testExecutionResult);
    }

    private TestExecutionSummary executeTests(final EngineExecutionListener parentEngineExecutionListener) {
        final SummaryGeneratingListener listener = new SummaryGeneratingListener();
        executionOrchestrator.execute(discoveryResult, parentEngineExecutionListener, listener);
        return listener.getSummary();
    }

    private TestExecutionResult computeTestExecutionResult(final TestExecutionSummary summary, final ThrowableCollector throwableCollector) {
        if (throwableCollector.isNotEmpty()) {
            return TestExecutionResult.failed(throwableCollector.getThrowable());
        }
        if (failIfNoTests && summary.getTestsFoundCount() == 0) {
            return TestExecutionResult.failed(new JUnitException(String.format("Suite [%s] did not discover any tests", testSuiteClass.getName())));
        }
        return TestExecutionResult.successful();
    }

    private void executeMethods(final List<Method> methods, final ThrowableCollector throwableCollector) {
        methods.forEach(method -> throwableCollector.execute(() -> ReflectionUtils.invokeMethod(method, null)));
    }
}
