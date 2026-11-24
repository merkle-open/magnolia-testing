package com.merkle.oss.magnolia.testing.servlet;

import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
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

@Request
public class ServletContextProvider implements Provider<ServletContext> {
    private final Request request;
    private final Class<?> testClass;

    @Inject
    public ServletContextProvider(final Context context) {
        request = context.getAnnotation(Request.class).findAny().orElseGet(() ->
                ServletContextProvider.class.getAnnotation(Request.class)
        );
        testClass = context.getClazz();
    }

    @Override
    public ServletContext get() {
        final ServletContext mock = Mockito.mock(ServletContext.class);
        Mockito.doAnswer(invocation -> request.contextPath()).when(mock).getContextPath();
        Mockito.doAnswer(invocation -> Optional.ofNullable(testClass.getResource(invocation.getArgument(0))).map(URL::getPath).orElse(null)).when(mock).getRealPath(Mockito.anyString());
        Mockito.doAnswer(invocation -> Collections.enumeration(Collections.emptySet())).when(mock).getInitParameterNames();
        Mockito.doAnswer(invocation -> Collections.enumeration(Collections.emptySet())).when(mock).getAttributeNames();
        return mock;
    }

    public HttpServletRequest getRequest() {
        final HttpServletRequest mock = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocation -> request.remoteAddr()).when(mock).getRemoteAddr();
        Mockito.doAnswer(invocation -> request.requestURI()).when(mock).getRequestURI();
        Mockito.doAnswer(invocation -> request.servletPath()).when(mock).getServletPath();
        Mockito.doAnswer(invocation -> request.contextPath()).when(mock).getContextPath();
        Mockito.doAnswer(invocation -> new StringBuffer(request.requestURL())).when(mock).getRequestURL();
        Mockito.doAnswer(invocation -> request.method().toString()).when(mock).getMethod();
        Mockito.doAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            return streamHeaders(key).findFirst().orElse(null);
        }).when(mock).getHeader(Mockito.anyString());
        Mockito.doAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            return Collections.enumeration(streamHeaders(key).collect(Collectors.toSet()));
        }).when(mock).getHeaders(Mockito.anyString());
        final HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(mock).getSession(Mockito.anyBoolean());
        Mockito.doReturn(session).when(mock).getSession();
        return mock;
    }

    private Stream<String> streamHeaders(final String key) {
        if (Objects.equals("Authorization", key)) {
            return Stream.of("Basic " + Base64.getEncoder().encodeToString((request.user().username() + ":" + request.user().password()).getBytes()));
        }
        return Stream.of(request.headers())
                .filter(header -> Strings.CS.equals(key, header.key()))
                .map(Request.Header::value);
    }

    public HttpServletResponse getResponse() {
        final HttpServletResponse mock = Mockito.mock(HttpServletResponse.class);
        return mock;
    }
}
