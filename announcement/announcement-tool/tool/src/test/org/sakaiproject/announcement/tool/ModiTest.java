package org.sakaiproject.announcement.tool;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Convenience base class for tests that need a kernel in a generic application context. Extend this class and add a
 * {@link ContextConfiguration} annotation with the locations (of XML bean files) or classes (Java configuration) set.
 *
 * If you need a web application context for MockMvc/WebClient, extend {@link ModiWebTest}.
 */
@ContextConfiguration(
        loader = ModiContextLoader.class,
        initializers = ModiInitializer.class,
        locations = {
            "classpath:org/sakaiproject/config/modi-configuration.xml",
            "classpath:org/sakaiproject/kernel/components.xml"
        })
public abstract class ModiTest {
}
