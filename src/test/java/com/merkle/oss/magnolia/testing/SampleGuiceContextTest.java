package com.merkle.oss.magnolia.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.merkle.oss.magnolia.testing.configuration.TestConfiguration;
import com.merkle.oss.magnolia.testing.repository.Repository;

@Repository(workspaces = {
        @Repository.Workspace(name = RepositoryConstants.WEBSITE, xml = "jcr.xml")
})
@ExtendWith(MagnoliaGuiceContextTestExtension.class)
class SampleGuiceContextTest {

    @Test
    void someMethod() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
    }

    @Test
    @TestConfiguration(components = {
            @TestConfiguration.Component(type = SomeInterface.class, implementation = SomeInterface.SomeOtherImplementation.class)
    })
    void someOtherMethod() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some other implementation!", someInterface.someMethod(node));
    }

    @Repository(
            nodeTypes = {@Repository.NodeTypesDefinition(cnd = "/mgnl-nodetypes/testing-nodetypes.cnd")},
            workspaces = {@Repository.Workspace(name = RepositoryConstants.WEBSITE, xml = "jcr-custom-nodetype.xml")}
    )
    @Test
    void customNodeType() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
        session.logout();
    }

    @Repository(workspaces = {@Repository.Workspace(name = "testing", xml = "jcr.xml", create = true)})
    @Test
    void customWorkspace() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession("testing");
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
        session.logout();
    }
}
