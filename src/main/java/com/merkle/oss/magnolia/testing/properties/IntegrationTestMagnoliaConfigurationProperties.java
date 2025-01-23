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

import com.google.inject.Inject;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.testing.Context;
import com.merkle.oss.magnolia.testing.configuration.TestConfiguration;

public class IntegrationTestMagnoliaConfigurationProperties extends DefaultMagnoliaConfigurationProperties {
    private final Path appRootDir;
    private final Context context;

    @Inject
    public IntegrationTestMagnoliaConfigurationProperties(
            final MagnoliaInitPaths initPaths,
            final ModuleRegistry moduleRegistry,
            final MagnoliaPropertiesResolver resolver,
            final SystemPropertySource systemPropertySource,
            final EnvironmentPropertySource environmentPropertySource,
            final Path appRootDir,
            final Context context
    ) {
        super(initPaths, moduleRegistry, resolver, systemPropertySource, environmentPropertySource);
        this.appRootDir = appRootDir;
        this.context = context;
    }

    @Override
    public void init() throws Exception {
        sources.addAll(getInitialPropertySources(appRootDir, context));
        super.init();
    }

    @Override
    protected String parseStringValue(final String strVal, final Set<String> visitedPlaceholders) {
        return super.parseStringValue(strVal, visitedPlaceholders);
    }

    public static List<PropertySource> getInitialPropertySources(final Path appRootDir, final Context context) throws IOException {
        return Stream.concat(
                getCustomProperties(context)
                        .filter(path -> IntegrationTestMagnoliaConfigurationProperties.class.getResource(path) != null)
                        .map(path -> Exceptions.wrap().get(() -> new ReferencingClasspathPropertySource(path, appRootDir))),
                Stream.of(
                        new TestPropertySource(appRootDir),
                        new ReferencingClasspathPropertySource("/default-test-magnolia.properties", appRootDir)
                )
        ).toList();
    }

    private static Stream<String> getCustomProperties(final Context context) {
        return context.getAnnotation(TestConfiguration.class)
                .map(TestConfiguration::magnoliaProperties)
                .flatMap(Arrays::stream);
    }
}