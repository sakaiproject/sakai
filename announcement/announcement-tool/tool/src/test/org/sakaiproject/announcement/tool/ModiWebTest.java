package org.sakaiproject.announcement.tool;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Convenience base class for tests that need a kernel in a web application context. Extend this class and add a
 * {@link ContextConfiguration} annotation with the locations (of XML bean files) or classes (Java configuration) set.
 */
@WebAppConfiguration
@ContextConfiguration(loader = ModiWebContextLoader.class)
public abstract class ModiWebTest extends ModiTest {
}
