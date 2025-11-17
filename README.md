# Magnolia Testing

Provides integration testing capabilities for magnolia projects. 

## Requirements
* Java 17
* Magnolia >= 6.3

## Setup

- Add Maven dependency:
  ```xml
  <dependency>
      <groupId>com.merkle.oss.magnolia</groupId>
      <artifactId>magnolia-testing</artifactId>
      <version>0.1.1</version>
  </dependency>
  ```
- Add a magnolia module descriptor in your `src/test/resources/META-INF/magnolia` directory (Bindings can differ from non-test setup).

### Cluster config (only necessary for shared/cluster repo setup )
configure the following properties (see [Custom Magnolia properties](#Custom-Magnolia-properties))
```properties
magnolia.repositories.config=classpath:/repository/repositories-cluster.xml
```

## [Integration Test](src/test/java/com/merkle/oss/magnolia/testing/SampleIntegrationTest.java)
Creates guice context and starts all magnolia modules. Can import jcr exports (xml files) into repository using [@Repository](src/main/java/com/merkle/oss/magnolia/testing/repository/Repository.java) annotation.<br>
There's also a `MagnoliaIntegrationBeforeAllTestExtension` which runs all tests in the same context (for better performance).
```java
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.merkle.oss.magnolia.testing.MagnoliaIntegrationTestExtension;
import com.merkle.oss.magnolia.testing.repository.Repository;

@ExtendWith(MagnoliaIntegrationTestExtension.class)
class SampleIntegrationTest {

    @Repository(workspaces = {@Repository.Workspace(name = RepositoryConstants.WEBSITE, xml = "jcr.xml")})
    @Test
    void someMethod() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        final Node node = session.getRootNode().getNode("0");
        final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
        assertEquals("some value 42!", someInterface.someMethod(node));
        session.logout();
    }
}
```

## [Guice Context Test](src/test/java/com/merkle/oss/magnolia/testing/SampleGuiceContextTest.java)
Only creates guice context, but doesn't start magnolia modules. Can import jcr exports (xml files) into repository, create workspaces and import nodeTypes using [@Repository](src/main/java/com/merkle/oss/magnolia/testing/repository/Repository.java) annotation.<br>
There's also a `MagnoliaGuiceContextBeforeAllTestExtension` which runs all tests in the same context (for better performance).

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.merkle.oss.magnolia.testing.MagnoliaGuiceContextTestExtension;
import com.merkle.oss.magnolia.testing.repository.Repository;

@Repository(
        nodeTypes = { @Repository.NodeTypesDefinition(cnd = "/mgnl-nodetypes/testing-nodetypes.cnd") },
        workspaces = { @Repository.Workspace(name = "testing", xml = "jcr-custom-nodetype.xml", create = true) }
)
@ExtendWith(MagnoliaGuiceContextTestExtension.class)
class SampleGuiceContextTest {

  @Test
  void someMethod() throws RepositoryException {
    final Session session = MgnlContext.getJCRSession("testing");
    final Node node = session.getRootNode().getNode("0");
    final SomeInterface someInterface = Components.getComponent(SomeInterface.class);
    assertEquals("some value 42!", someInterface.someMethod(node));
  }
}
```

## [Test suite](src/test/java/com/merkle/oss/magnolia/testing/IntegrationTestSuite.java)
Since the initialization can be quite slow it is possible to create suites, where the initialization is done once before all tests are executed.<br>
<b>NOTE:</b> While test specific repository imports are possible, test specific configuration / binding is not possible!

```java
import java.util.List;
import com.merkle.oss.magnolia.testing.suite.MagnoliaTestSuite;

@MagnoliaTestSuite(testClassProvider = IntegrationTestSuite.TestClassProvider.class)
public class SampleTestSuite {
  public static class TestClassProvider implements MagnoliaTestSuite.TestClassProvider {
      @Override
      public List<Class<?>> get() {
          //TODO implement
          return List.of();
      }
  }
}
```

## Custom Magnolia properties
Custom property files can be specified using the [`@TestConfiguration`](src/main/java/com/merkle/oss/magnolia/testing/configuration/TestConfiguration.java) annotation.

```java
import com.merkle.oss.magnolia.testing.MagnoliaGuiceContextTestExtension;
import com.merkle.oss.magnolia.testing.configuration.TestConfiguration;

@TestConfiguration(magnoliaProperties = "/magnolia-testing.properties")
@ExtendWith(MagnoliaGuiceContextTestExtension.class)
class SampleGuiceContextTest {}
```

### Placeholders
- `${resource.home}` is replaced by your projects `src/test/resources` directory.
  - e.g. `magnolia.repositories.config=${resource.home}/repositories.xml`
- `classpath:` is replaced by any classpath resource
  - e.g. `classpath:/repository/InMemoryJcrRepositoryConfiguration.xml`
- `magnolia.app.rootdir` is replaced by a random generated temporary folder (for each test)
  - e.g. `magnolia.home=${magnolia.app.rootdir}`

## Test specific component bindings
Test specific component bindings can be configured using the [`@TestConfiguration`](src/main/java/com/merkle/oss/magnolia/testing/configuration/TestConfiguration.java) annotation.

```java
import org.junit.jupiter.api.extension.ExtendWith;

import com.merkle.oss.magnolia.testing.MagnoliaIntegrationTestExtension;
import com.merkle.oss.magnolia.testing.configuration.TestConfiguration;

@ExtendWith(MagnoliaIntegrationTestExtension.class)
class SampleGuiceContextTest {

  @Test
  @TestConfiguration(components = {
          @TestConfiguration.Component(type = SomeInterface.class, implementation = SomeInterface.SomeOtherImplementation.class)
  })
  void someOtherMethod() {
    ...
  }
}
```

## Annotation class reference
With the `@AnnotationClassReference` annotation it is possible to reference configurations / repository imports (e.g. multiple tests can reference the same TestConfiguration)<br>

```java
import org.junit.jupiter.api.extension.ExtendWith;
import com.merkle.oss.magnolia.testing.MagnoliaIntegrationTestExtension;
import com.merkle.oss.magnolia.testing.configuration.TestConfiguration;
import com.merkle.oss.magnolia.testing.repository.Repository;

@TestConfiguration(...)
@Repository(...)
@ExtendWith(MagnoliaIntegrationTestExtension.class)
public abstract class AbstractIntegrationTest {}
```

```java
import com.merkle.oss.magnolia.testing.configuration.AnnotationClassReference;
import com.merkle.oss.magnolia.testing.suite.MagnoliaTestSuite;

@MagnoliaTestSuite(testClassProvider =...)
@AnnotationClassReference(AbstractIntegrationTest.class)
public class IntegrationTestSuite {}
```
