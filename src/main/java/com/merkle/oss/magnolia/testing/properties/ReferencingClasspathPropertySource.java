package com.merkle.oss.magnolia.testing.properties;

import info.magnolia.init.properties.ClasspathPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class ReferencingClasspathPropertySource extends ClasspathPropertySource {
    private final Path appRootDir;

    public ReferencingClasspathPropertySource(final String path, final Path appRootDir) throws IOException {
        super(path);
        this.appRootDir = appRootDir;
    }

    @Override
    public String getProperty(final String key) {
        final String value = super.getProperty(key);
        if(value != null && value.startsWith("classpath:")) {
            try {
                final String classpathResource = StringUtils.removeStart(value, "classpath:");
                final String extension = FilenameUtils.getExtension(classpathResource);
                final String baseName = FilenameUtils.getBaseName(classpathResource);
                final File tempFile = Files.createTempFile(appRootDir, baseName, "."+extension).toFile();
                FileUtils.copyURLToFile(getClass().getResource(classpathResource), tempFile);
                return tempFile.getAbsolutePath();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }
}
