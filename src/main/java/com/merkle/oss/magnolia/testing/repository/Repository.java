package com.merkle.oss.magnolia.testing.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Repository {
	Workspace[] workspaces();
	NodeTypesDefinition[] nodeTypes() default {};

	@interface Workspace {
		String name();
		String xml();
		String repositoryId() default "magnolia";
		boolean create() default false;
	}

	@interface NodeTypesDefinition {
		String repositoryId() default "magnolia";
		String cnd();
	}
}
