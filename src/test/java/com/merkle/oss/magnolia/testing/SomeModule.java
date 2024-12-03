package com.merkle.oss.magnolia.testing;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import java.util.concurrent.atomic.AtomicBoolean;

public class SomeModule implements ModuleLifecycle {
    private final AtomicBoolean started = new AtomicBoolean();

    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        started.set(true);
    }

    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        started.set(false);
    }

    public boolean isStarted() {
        return started.get();
    }
}
