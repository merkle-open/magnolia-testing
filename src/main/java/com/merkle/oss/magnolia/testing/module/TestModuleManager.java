package com.merkle.oss.magnolia.testing.module;

import info.magnolia.health.HealthCheckRegistry;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleLifecycleContextImpl;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.repository.RepositoryManager;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.merkle.oss.magnolia.testing.configuration.TestModuleVersionHandler;

public class TestModuleManager extends ModuleManagerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final InstallContextImpl installContext;

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
        this.installContext = installContext;
    }

    @Override
    public void performInstallOrUpdate() {
        super.performInstallOrUpdate();

        final ModuleDefinition moduleDefinition = new ModuleDefinition();
        moduleDefinition.setName("testing");
        moduleDefinition.setVersion(Version.UNDEFINED_DEVELOPMENT_VERSION);
        installContext.setCurrentModule(moduleDefinition);
        final List<Delta> deltas = new TestModuleVersionHandler().getDeltas(installContext, null);
        installOrUpdateModule(new ModuleAndDeltas(moduleDefinition, Version.UNDEFINED_DEVELOPMENT_VERSION, deltas), installContext);
        installContext.setCurrentModule(null);
    }

    @Override
    protected void startModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
        final StopWatch watch = new StopWatch();
        watch.start();
        super.startModule(moduleInstance, moduleDefinition, lifecycleContext);
        watch.stop();
        LOG.debug("Starting module {} took {}ms", moduleDefinition.getName(),  watch.getDuration().toMillis());
    }
}
