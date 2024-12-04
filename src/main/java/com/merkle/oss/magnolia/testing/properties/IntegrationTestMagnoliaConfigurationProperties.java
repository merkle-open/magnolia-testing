package com.merkle.oss.magnolia.testing.properties;

import info.magnolia.init.DefaultMagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaInitPaths;
import info.magnolia.init.MagnoliaPropertiesResolver;
import info.magnolia.init.PropertySource;
import info.magnolia.init.properties.EnvironmentPropertySource;
import info.magnolia.init.properties.SystemPropertySource;
import info.magnolia.module.ModuleRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.inject.Inject;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.testing.configuration.TestConfiguration;

public class IntegrationTestMagnoliaConfigurationProperties extends DefaultMagnoliaConfigurationProperties {
    private final Path appRootDir;
    private final ExtensionContext extensionContext;

    @Inject
    public IntegrationTestMagnoliaConfigurationProperties(
            final MagnoliaInitPaths initPaths,
            final ModuleRegistry moduleRegistry,
            final MagnoliaPropertiesResolver resolver,
            final SystemPropertySource systemPropertySource,
            final EnvironmentPropertySource environmentPropertySource,
            final Path appRootDir,
            final ExtensionContext extensionContext
    ) {
        super(initPaths, moduleRegistry, resolver, systemPropertySource, environmentPropertySource);
        this.appRootDir = appRootDir;
        this.extensionContext = extensionContext;
    }

    @Override
    public void init() throws Exception {
        sources.addAll(getInitialPropertySources(appRootDir, extensionContext));
        super.init();
    }

    @Override
    protected String parseStringValue(final String strVal, final Set<String> visitedPlaceholders) {
        return super.parseStringValue(strVal, visitedPlaceholders);
    }

    public static List<PropertySource> getInitialPropertySources(final Path appRootDir, final ExtensionContext extensionContext) throws IOException {
        return Stream.concat(
                getCustomProperties(extensionContext)
                        .filter(path -> IntegrationTestMagnoliaConfigurationProperties.class.getResource(path) != null)
                        .map(path -> Exceptions.wrap().get(() -> new ReferencingClasspathPropertySource(path, appRootDir))),
                Stream.of(
                        new TestPropertySource(appRootDir),
                        new ReferencingClasspathPropertySource("/default-test-magnolia.properties", appRootDir)
                )
        ).toList();
    }

    private static Stream<String> getCustomProperties(final ExtensionContext extensionContext) {
        return Stream
                .of(extensionContext.getTestMethod(), extensionContext.getTestClass())
                .map(method -> method.map(m -> m.getAnnotation(TestConfiguration.class)))
                .flatMap(Optional::stream)
                .map(TestConfiguration::magnoliaProperties)
                .flatMap(Arrays::stream);
    }
}