package org.sakaiproject.test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Convenience base class for tests that need a kernel in a web application context. Extend this class and add a
 * {@link ContextConfiguration} annotation with the locations (of XML bean files) or classes (Java configuration) set.
 */
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(name = "kernel", loader = ModiWebContextLoader.class),
        @ContextConfiguration(name = "portal", classes = ModiPortalConfig.class)
})
public abstract class ModiWebTest extends ModiTest {
}
