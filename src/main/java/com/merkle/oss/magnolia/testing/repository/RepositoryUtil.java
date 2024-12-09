package com.merkle.oss.magnolia.testing.repository;

import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RepositoryManager repositoryManager;

    public RepositoryUtil() {
        this(Components.getComponent(RepositoryManager.class));
    }

    public RepositoryUtil(final RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public void load(final ExtensionContext extensionContext) throws IOException, RepositoryException {
        final Repository repository = extensionContext.getTestMethod().map(method -> method.getAnnotation(Repository.class))
                .or(() -> extensionContext.getTestClass().map(clazz -> clazz.getAnnotation(Repository.class)))
                .orElse(null);
        if(repository != null) {
            load(extensionContext.getRequiredTestClass(), repository);
        }
    }

    private void load(final Class<?> testClass, final Repository repository) throws IOException, RepositoryException {
        for (Repository.NodeTypesDefinition nodeTypesDefinition : repository.nodeTypes()) {
            repositoryManager.getRepositoryProvider(nodeTypesDefinition.repositoryId()).registerNodeTypes(nodeTypesDefinition.cnd());
        }
        for (Repository.Workspace workspace : repository.workspaces()) {
            if (!repositoryManager.hasWorkspace(workspace.name()) && workspace.create()) {
                repositoryManager.loadWorkspace(workspace.repositoryId(), workspace.name());
            }
            final Session session = repositoryManager.getSystemSession(workspace.name());
            session.importXML("/", testClass.getResourceAsStream(workspace.xml()), ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
            session.save();
            session.logout();
        }
    }

    public void dump(final String workspace) throws RepositoryException {
        final Session session = repositoryManager.getSystemSession(workspace);
        dump(session.getRootNode());
        session.logout();
    }

    /**
     * Recursively outputs the contents of the given node.
     */
    public void dump(final Node node) throws RepositoryException {
        // First output the node path
        LOG.info(node.getPath());
        // Skip the virtual (and large!) jcr:system subtree
        if (node.getName().equals("jcr:system")) {
            return;
        }

        // Then output the properties
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            if (property.getDefinition().isMultiple()) {
                // A multi-valued property, print all values
                Value[] values = property.getValues();
                for (int i = 0; i < values.length; i++) {
                    LOG.info(property.getPath() + " = " + values[i].getString());
                }
            } else {
                // A single-valued property
                LOG.info(property.getPath() + " = " + property.getString());
            }
        }

        // Finally output all the child nodes recursively
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            dump(nodes.nextNode());
        }
    }
}
