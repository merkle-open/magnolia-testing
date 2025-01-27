package com.merkle.oss.magnolia.testing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;

import com.merkle.oss.magnolia.testing.configuration.AnnotationClassReference;

public interface Context {
    Class<?> getClazz();
    Optional<Method> getMethod();

    default <T extends Annotation> Stream<T> getAnnotation(final Class<T> annotationClass) {
        final Class<?> referencedClass = Optional.ofNullable(getClazz().getAnnotation(AnnotationClassReference.class)).map(AnnotationClassReference::value).orElse(null);
        return Stream
                .of(getMethod(), Optional.of(referencedClass != null ? referencedClass : getClazz()))
                .map(method -> method.map(m -> m.getAnnotation(annotationClass)))
                .flatMap(Optional::stream);
    }

    class TestContextWrapper implements Context {
        private final ExtensionContext extensionContext;

        public TestContextWrapper(final ExtensionContext extensionContext) {
            this.extensionContext = extensionContext;
        }

        @Override
        public Class<?> getClazz() {
            return extensionContext.getRequiredTestClass();
        }

        @Override
        public Optional<Method> getMethod() {
            return extensionContext.getTestMethod();
        }
    }
}
