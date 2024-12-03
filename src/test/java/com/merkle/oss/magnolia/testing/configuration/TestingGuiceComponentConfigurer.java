package com.merkle.oss.magnolia.testing.configuration;

import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;

import java.time.ZoneId;

public class TestingGuiceComponentConfigurer extends AbstractGuiceComponentConfigurer {

	@Override
	protected void configure() {
		super.configure();
		binder().bind(ZoneId.class).toProvider(() -> ZoneId.of("Europe/Zurich"));
	}
}
