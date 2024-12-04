package com.merkle.oss.magnolia.testing.servlet;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockFilterChain implements FilterChain {
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response) {}
}
