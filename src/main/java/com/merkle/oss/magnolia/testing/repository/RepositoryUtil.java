package com.merkle.oss.magnolia.testing.repository;

import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.testing.Context;

public class RepositoryUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RepositoryManager repositoryManager;
    public static final Predicate<Node> JCR_SYSTEM_NODE_PREDICATE = node ->
            Exceptions.wrap().get(node::getName).equals(JcrConstants.JCR_SYSTEM);

    public RepositoryUtil() {
        this(Components.getComponent(RepositoryManager.class));
    }

    public RepositoryUtil(final RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public void load(final Context context) throws IOException, RepositoryException {
        for(final Repository repository: context.getAnnotation(Repository.class).toList()) {
            load(context.getClazz(), repository);
        }
    }

    private void load(final Class<?> testClass, final Repository repository) throws IOException, RepositoryException {
        for (Repository.NodeTypesDefinition nodeTypesDefinition : repository.nodeTypes()) {
            repositoryManager.getRepositoryProvider(nodeTypesDefinition.repositoryId()).registerNodeTypes(nodeTypesDefinition.cnd());
        }
        createAndClearWorkspaces(repository);
        importWorkspaceContent(testClass, repository);
    }

    private void createAndClearWorkspaces(final Repository repository) throws RepositoryException {
        for (Repository.Workspace workspace : repository.workspaces()) {
            if (!repositoryManager.hasWorkspace(workspace.name())) {
                repositoryManager.loadWorkspace(workspace.repositoryId(), workspace.name());
            }
            if (workspace.clear()) {
                final Session session = repositoryManager.getSystemSession(workspace.name());
                deleteAllChildren(session.getRootNode());
                session.save();
                session.logout();
            }
        }
    }

    private void importWorkspaceContent(final Class<?> testClass, final Repository repository) throws RepositoryException, IOException {
        for (Repository.Workspace workspace : repository.workspaces()) {
            final URL resource = Optional.ofNullable(testClass.getResource(workspace.xml())).orElseThrow(() ->
                    new IllegalArgumentException("resource " + workspace.xml() + " is not present!")
            );
            final Session session = repositoryManager.getSystemSession(workspace.name());
            session.importXML(workspace.path(), resource.openStream(), workspace.importUUIDBehavior());
            session.save();
            session.logout();
        }
    }

    private void deleteAllChildren(final Node node) throws RepositoryException {
        final NodeIterator nodes = node.getNodes();
        while(nodes.hasNext()) {
            final Node child = nodes.nextNode();
            if (!JCR_SYSTEM_NODE_PREDICATE.test(child)) {
                child.remove();
            }
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
        if (JCR_SYSTEM_NODE_PREDICATE.test(node)) {
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
