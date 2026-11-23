package com.merkle.oss.magnolia.testing.servlet;

import info.magnolia.cms.security.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Request {
	String requestURI() default "/mock-uri";
	String servletPath() default "/mock-uri";
	String contextPath() default "/";
	String requestURL() default "https://mock-domain.com/mock-uri";
	String remoteAddr() default "127.0.0.1";
	HttpMethod method() default HttpMethod.GET;
	User user() default @User(username = "superuser", password = "superuser");
	Header[] headers() default {};

	@interface Header {
		String key();
		String value();
	}

	@interface User {
		String username();
		String password();
	}
}
