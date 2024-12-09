package com.merkle.oss.magnolia.testing.configuration;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.filters.MgnlMainFilter;
import info.magnolia.cms.util.CustomFilterConfig;
import info.magnolia.context.AbstractSystemContext;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaServletContextListener;
import info.magnolia.license.LicenseManager;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.ImplementationConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.TestMagnoliaConfigurationProperties;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.CreationException;
import com.google.inject.Stage;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.testing.module.LicenseManagerProvider;
import com.merkle.oss.magnolia.testing.properties.IntegrationTestMagnoliaConfigurationProperties;
import com.merkle.oss.magnolia.testing.servlet.MockFilterChain;
import com.merkle.oss.magnolia.testing.servlet.ServletContextProvider;

public class MagnoliaIntegrationTestInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void init(final ExtensionContext extensionContext) throws Exception {
        final StopWatch watch = new StopWatch();
        watch.start();
        try {
            final Path appRootDir = Exceptions.wrap().get(() -> Files.createTempDirectory("magnolia-test"));
            final GuiceComponentProvider platformComponentProvider = getPlatformComponentProvider(appRootDir, extensionContext);
            final RepositoryManager repositoryManager = platformComponentProvider.getComponent(RepositoryManager.class);
            repositoryManager.init();
            final ModuleManager moduleManager = platformComponentProvider.getComponent(ModuleManager.class);
            moduleManager.loadDefinitions();
            MgnlContext.setInstance(Components.getComponent(SystemContext.class));

            final IntegrationTestMagnoliaConfigurationProperties properties = platformComponentProvider.newInstance(IntegrationTestMagnoliaConfigurationProperties.class, appRootDir);
            properties.init();
            // Connect legacy properties to the MagnoliaConfigurationProperties object
            SystemProperty.setMagnoliaConfigurationProperties(properties);
            //ConfigLoader.JAAS_PROPERTYNAME
            System.setProperty("java.security.auth.login.config", properties.getProperty("jaas.config"));

            final GuiceComponentProvider systemComponentProvider = getSystemComponentProvider(extensionContext, platformComponentProvider, properties);
            getMainComponentProvider(extensionContext, systemComponentProvider, properties);
        } catch (CreationException e) {
            throw new RuntimeException("Failed to init: " + e.getErrorMessages(), e);
        }
        watch.stop();
        LOG.debug("Initialization took {}ms", watch.getDuration().toMillis());
    }

    public void destroy() {
        ((AbstractSystemContext) Components.getComponent(SystemContext.class)).getRepositoryStrategy().release();
        MgnlContext.setInstance(null);
    }

    public void start(final boolean executeFilterChain) throws ModuleManagementException, ServletException, IOException {
        final StopWatch watch = new StopWatch();
        watch.start();
        System.setProperty("productionMode", "true"); //com.vaadin.server.Constants.SERVLET_PARAMETER_PRODUCTION_MODE
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        final ModuleManager moduleManager = Components.getComponent(ModuleManager.class);
        moduleManager.checkForInstallOrUpdates();
        moduleManager.performInstallOrUpdate();
        moduleManager.startModules();
        if(executeFilterChain) {
            executeFilterChain();
        }
        watch.stop();
        LOG.debug("Start took {}ms", watch.getDuration().toMillis());
    }

    private void executeFilterChain() throws ServletException, IOException {
        final ServletContextProvider servletContextProvider = Components.getComponent(ServletContextProvider.class);
        final MgnlMainFilter mainFilter = new MgnlMainFilter();
        mainFilter.init(new CustomFilterConfig("magnoliaFilterChain", Components.getComponent(ServletContext.class)));
        mainFilter.doFilter(servletContextProvider.getRequest(), servletContextProvider.getResponse(), new MockFilterChain());
    }

    public void stop() {
        final StopWatch watch = new StopWatch();
        watch.start();
        final ModuleManager moduleManager = Components.getComponent(ModuleManager.class);
        moduleManager.stopModules();
        ((AbstractSystemContext) Components.getComponent(SystemContext.class)).getRepositoryStrategy().release();
        MgnlContext.setInstance(null);
        watch.stop();
        LOG.debug("Stop took {}ms", watch.getDuration().toMillis());
    }

    private GuiceComponentProvider getPlatformComponentProvider(final Path appRootDir, final ExtensionContext extensionContext) throws IOException {
        final TestMagnoliaConfigurationProperties properties = new TestMagnoliaConfigurationProperties(IntegrationTestMagnoliaConfigurationProperties.getInitialPropertySources(appRootDir, extensionContext));
        final ComponentProviderConfiguration config = new ComponentProviderConfigurationBuilder().readConfiguration(List.of(
                MagnoliaServletContextListener.DEFAULT_PLATFORM_COMPONENTS_CONFIG_LOCATION,
                "/configuration/platform-components.xml"
        ), "platform");

        config.registerProvider(ServletContext.class, ServletContextProvider.class);
        applyAnnotationComponents(extensionContext, TestConfiguration.Component.Provider.PLATFORM, config);
        config.registerInstance(MagnoliaConfigurationProperties.class, properties);
        config.registerInstance(ExtensionContext.class, extensionContext);
        final GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(config);
        builder.inStage(Stage.PRODUCTION);
        builder.exposeGlobally();
        return builder.build();
    }

    private GuiceComponentProvider getSystemComponentProvider(final ExtensionContext extensionContext, final GuiceComponentProvider parent, final MagnoliaConfigurationProperties properties) {
        final ComponentProviderConfiguration config = merge(
                new ComponentProviderConfigurationBuilder().readConfiguration(List.of("/configuration/system-components.xml"), "system"),
                new ComponentProviderConfigurationBuilder().getComponentsFromModules(
                        "system",
                        parent.getComponent(ModuleRegistry.class).getModuleDefinitions()
                )
        );
        applyAnnotationComponents(extensionContext, TestConfiguration.Component.Provider.SYSTEM, config);
        config.registerInstance(IntegrationTestMagnoliaConfigurationProperties.class, properties);
        config.registerInstance(MagnoliaConfigurationProperties.class, properties);
        final GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(config);
        builder.withParent(parent);
        builder.exposeGlobally();
        return builder.build();
    }

    private GuiceComponentProvider getMainComponentProvider(final ExtensionContext extensionContext, final GuiceComponentProvider parent, final MagnoliaConfigurationProperties properties) {
        final ComponentProviderConfiguration config = new ComponentProviderConfigurationBuilder().getComponentsFromModules(
                "main",
                parent.getComponent(ModuleRegistry.class).getModuleDefinitions()
        );
        config.registerInstance(MagnoliaConfigurationProperties.class, properties);
        config.registerProvider(LicenseManager.class, LicenseManagerProvider.class);
        applyAnnotationComponents(extensionContext, TestConfiguration.Component.Provider.MAIN, config);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(config);
        builder.withParent((GuiceComponentProvider) Components.getComponentProvider());
        builder.exposeGlobally();
        return builder.build();
    }

    private ComponentProviderConfiguration merge(final ComponentProviderConfiguration config1, final ComponentProviderConfiguration config2) {
        final ComponentProviderConfiguration merged = new ComponentProviderConfiguration();
        Stream
                .of(config1, config2)
                .flatMap(config -> config.getAnnotatedComponents().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (c1, c2) -> c1))
                .values()
                .forEach(merged::addComponent);
        Stream
                .of(config1, config2)
                .flatMap(config -> config.getAnnotatedTypeMappings().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (c1, c2) -> c1))
                .forEach(merged::addTypeMapping);
        return merged;
    }

    private void applyAnnotationComponents(final ExtensionContext extensionContext, final TestConfiguration.Component.Provider provider, final ComponentProviderConfiguration config) {
        getComponents(extensionContext)
                .filter(component -> provider.equals(component.provider()))
                .map(component -> {
                    final ImplementationConfiguration configuration = new ImplementationConfiguration<>();
                    configuration.setType(component.type());
                    configuration.setImplementation(component.implementation());
                    configuration.setScope(TestConfiguration.Component.Scope.SINGLETON.equals(component.scope()) ? "singleton" : null);
                    configuration.setLazy(component.lazy());
                    return configuration;
                })
                .forEach(config::addComponent);
    }

    private Stream<TestConfiguration.Component> getComponents(final ExtensionContext extensionContext) {
        return extensionContext.getTestMethod().map(method -> method.getAnnotation(TestConfiguration.class)).or(() ->
                extensionContext.getTestClass().map(clazz -> clazz.getAnnotation(TestConfiguration.class))
        ).stream().map(TestConfiguration::components).flatMap(Arrays::stream);
    }
}
