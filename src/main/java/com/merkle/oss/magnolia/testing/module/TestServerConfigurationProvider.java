package com.merkle.oss.magnolia.testing.module;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.transformer.BeanTypeResolver;
import info.magnolia.util.PreConfiguredBeanUtils2;

import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;

import com.google.inject.Provider;

public class TestServerConfigurationProvider implements Provider<ServerConfiguration> {
    @Override
    public ServerConfiguration get() {
        try {
            final Node2BeanTransformerImpl transformer = new Node2BeanTransformerImpl(Components.getComponent(PreConfiguredBeanUtils2.class), Components.getComponent(BeanTypeResolver.class)) {
                @Override
                public Object newBeanInstance(TransformationState state, Map<String, Object> properties, ComponentProvider componentProvider) {
                    return new ServerConfiguration();
                }
            };
            final Node node = MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getNode("/server");
            return (ServerConfiguration) Components.getComponent(Node2BeanProcessor.class).toBean(node, false, transformer, Components.getComponentProvider());
        } catch (Exception e) {
            final ServerConfiguration config = new ServerConfiguration();
            config.setInstanceUuid(UUID.randomUUID().toString());
            return config;
        }
    }
}
