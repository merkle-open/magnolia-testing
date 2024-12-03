package com.merkle.oss.magnolia.testing.module;

import info.magnolia.health.HealthCheckRegistry;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleLifecycleContextImpl;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.repository.RepositoryManager;

import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;

import com.google.inject.Inject;

public class TestModuleManager extends ModuleManagerImpl {

    @Inject
    public TestModuleManager(
            final InstallContextImpl installContext,
            final Set<ModuleDefinitionReader> moduleDefinitionReaders,
            final ModuleRegistry moduleRegistry,
            final DependencyChecker dependencyChecker,
            final Node2BeanProcessor nodeToBean,
            final RepositoryManager repositoryManager,
            final HealthCheckRegistry healthCheckRegistry
    ) {
        super(installContext, moduleDefinitionReaders, moduleRegistry, dependencyChecker, nodeToBean, repositoryManager, healthCheckRegistry);
    }

    @Override
    protected void startModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
        final StopWatch watch = new StopWatch();
        watch.start();
        super.startModule(moduleInstance, moduleDefinition, lifecycleContext);
        watch.stop();
        System.out.println("Starting module " + moduleDefinition.getName() + " took " + watch.getDuration().toMillis() + "ms");
    }
}
