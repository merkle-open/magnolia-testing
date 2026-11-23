package com.merkle.oss.magnolia.testing.servlet;

import java.util.Base64;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Strings;
import org.mockito.Mockito;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.merkle.oss.magnolia.testing.Context;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface ServletContextProvider extends Provider<ServletContext> {
    ServletContext get();
    HttpServletRequest getRequest();
    HttpServletResponse getResponse();

    @Request
    class DefaultServletContextProvider implements ServletContextProvider {
        private final Request request;

        @Inject
        public DefaultServletContextProvider(final Context context) {
            request = context.getAnnotation(Request.class).findAny().orElseGet(() ->
                    DefaultServletContextProvider.class.getAnnotation(Request.class)
            );
        }

        @Override
        public ServletContext get() {
            final ServletContext mock = Mockito.mock(ServletContext.class);
            Mockito.doReturn(request.contextPath()).when(mock).getContextPath();
            Mockito.doReturn("").when(mock).getRealPath(Mockito.anyString());
            Mockito.doReturn(Collections.enumeration(Collections.emptySet())).when(mock).getInitParameterNames();
            Mockito.doReturn(Collections.enumeration(Collections.emptySet())).when(mock).getAttributeNames();
            return mock;
        }

        @Override
        public HttpServletRequest getRequest() {
            final HttpServletRequest mock = Mockito.mock(HttpServletRequest.class);
            Mockito.doReturn(request.remoteAddr()).when(mock).getRemoteAddr();
            Mockito.doReturn(request.requestURI()).when(mock).getRequestURI();
            Mockito.doReturn(request.servletPath()).when(mock).getServletPath();
            Mockito.doReturn(request.contextPath()).when(mock).getContextPath();
            Mockito.doReturn(new StringBuffer(request.requestURL())).when(mock).getRequestURL();
            Mockito.doReturn(request.method().toString()).when(mock).getMethod();
            Mockito.doAnswer(invocation -> {
                final String key = invocation.getArgument(0);
                return streamHeaders(key).findFirst().orElse(null);
            }).when(mock).getHeader(Mockito.anyString());
            Mockito.doAnswer(invocation -> {
                final String key = invocation.getArgument(0);
                return Collections.enumeration(streamHeaders(key).collect(Collectors.toSet()));
            }).when(mock).getHeaders(Mockito.anyString());
            Mockito.doReturn("Basic " + Base64.getEncoder().encodeToString((request.user().username()+":"+request.user().password()).getBytes())).when(mock).getHeader("Authorization");
            final HttpSession session = Mockito.mock(HttpSession.class);
            Mockito.doReturn(session).when(mock).getSession(Mockito.anyBoolean());
            Mockito.doReturn(session).when(mock).getSession();
            return mock;
        }

        private Stream<String> streamHeaders(final String key) {
            return Stream.of(request.headers())
                    .filter(header -> Strings.CS.equals(key, header.key()))
                    .map(Request.Header::value);
        }

        @Override
        public HttpServletResponse getResponse() {
            final HttpServletResponse mock = Mockito.mock(HttpServletResponse.class);
            return mock;
        }
    }
}
