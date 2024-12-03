package com.merkle.oss.magnolia.testing.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface TestConfiguration {
	String[] magnoliaProperties() default {};
	Component[] components() default {};

	@interface Component {
		Provider provider() default Provider.MAIN;
		Class<?> type();
		Class<?> implementation();
		Scope scope() default Scope.DEFAULT;
		boolean lazy() default false;

		enum Provider {
			MAIN,
			SYSTEM,
			PLATFORM
		}

		enum Scope {
			SINGLETON,
			DEFAULT
		}
	}
}
