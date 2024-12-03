package com.merkle.oss.magnolia.testing.properties;

import info.magnolia.init.PropertySource;

import java.nio.file.Path;
import java.util.Set;

public class TestPropertySource implements PropertySource {
    private final Path appRootDir;

    public TestPropertySource(final Path appRootDir) {
        this.appRootDir = appRootDir;
    }

    @Override
    public Set<String> getKeys() {
        return Set.of("magnolia.app.rootdir", "resource.home");
    }

    @Override
    public String getProperty(final String key) {
        return switch (key) {
            case "magnolia.app.rootdir" -> appRootDir.toFile().getAbsolutePath();
            case "resource.home" -> getClass().getClassLoader().getResource("").getFile();
            default -> null;
        };
    }

    @Override
    public boolean getBooleanProperty(final String key) {
        return false;
    }

    @Override
    public boolean hasProperty(final String key) {
        return getKeys().contains(key);
    }

    @Override
    public String describe() {
        return "magnolia integration test properties";
    }
}
