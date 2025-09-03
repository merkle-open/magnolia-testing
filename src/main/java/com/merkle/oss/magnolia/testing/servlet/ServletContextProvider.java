package com.merkle.oss.magnolia.testing.servlet;

import java.util.Base64;
import java.util.Collections;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mockito.Mockito;

import com.google.inject.Provider;

public interface ServletContextProvider extends Provider<ServletContext> {
    ServletContext get();
    HttpServletRequest getRequest();
    HttpServletResponse getResponse();

    class DefaultServletContextProvider implements ServletContextProvider {
        @Override
        public ServletContext get() {
            final ServletContext mock = Mockito.mock(ServletContext.class);
            Mockito.doReturn("/").when(mock).getContextPath();
            Mockito.doReturn("").when(mock).getRealPath(Mockito.anyString());
            Mockito.doReturn(Collections.enumeration(Collections.emptySet())).when(mock).getInitParameterNames();
            Mockito.doReturn(Collections.enumeration(Collections.emptySet())).when(mock).getAttributeNames();
            return mock;
        }

        @Override
        public HttpServletRequest getRequest() {
            final HttpServletRequest mock = Mockito.mock(HttpServletRequest.class);
            Mockito.doReturn("127.0.0.1").when(mock).getRemoteAddr();
            Mockito.doReturn("/mock-uri").when(mock).getRequestURI();
            Mockito.doReturn("/mock-uri").when(mock).getServletPath();
            Mockito.doReturn("/").when(mock).getContextPath();
            Mockito.doReturn(new StringBuffer("https://mock-domain.com/mock-uri")).when(mock).getRequestURL();
            Mockito.doReturn("GET").when(mock).getMethod();
            Mockito.doReturn("Basic " + Base64.getEncoder().encodeToString("superuser:superuser".getBytes())).when(mock).getHeader("Authorization");
            Mockito.doReturn(Collections.enumeration(Collections.emptySet())).when(mock).getHeaders(Mockito.anyString());
            final HttpSession session = Mockito.mock(HttpSession.class);
            Mockito.doReturn(session).when(mock).getSession(Mockito.anyBoolean());
            Mockito.doReturn(session).when(mock).getSession();
            return mock;
        }

        @Override
        public HttpServletResponse getResponse() {
            final HttpServletResponse mock = Mockito.mock(HttpServletResponse.class);
            return mock;
        }
    }
}
