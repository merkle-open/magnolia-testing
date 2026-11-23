package com.merkle.oss.magnolia.testing;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import info.magnolia.cms.security.User;
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
import com.merkle.oss.magnolia.testing.servlet.Request;

@ExtendWith(MagnoliaIntegrationTestExtension.class)
@TestConfiguration(magnoliaProperties = { "/magnolia-testing.properties" })
class SampleIntegrationTest {

    @Test
    void moduleStart() {
        final SomeModule someModule = Components.getComponent(SomeModule.class);
        assertTrue(someModule.isStarted());
    }

    @Repository(workspaces = {@Repository.Workspace(name = RepositoryConstants.WEBSITE, xml = "jcr.xml")})
    @Test
    void someMethod() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
    }

    @Repository(workspaces = {@Repository.Workspace(name = RepositoryConstants.WEBSITE, xml = "jcr-custom-nodetype.xml")})
    @Test
    void customNodeType() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
    }

    @Repository(workspaces = {@Repository.Workspace(name = "testing", xml = "jcr.xml")})
    @Test
    void customWorkspace() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession("testing");
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
    }

    @Request(user = @Request.User(username = "tester", password = "tester"))
    @Test
    void customUser() throws RepositoryException {
        final User user = MgnlContext.getUser();
        assertEquals("some value 42!", user.getName());
        assertEquals("some value 42!", user.getPassword());
    }
}
