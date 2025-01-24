package com.merkle.oss.magnolia.testing.suite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.jcr.ImportUUIDBehavior;

import org.junit.platform.commons.annotation.Testable;

import com.google.inject.Provider;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Testable
public @interface MagnoliaTestSuite {
	boolean failIfNoTests() default true;
	Class<? extends TestClassProvider> testClassProvider();
	boolean initializeMagnolia() default true;

	interface TestClassProvider {
		List<Class<?>> get();
	}
}
