package com.merkle.oss.magnolia.testing.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class MockFilterChain implements FilterChain {
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response) {}
}
