package org.sakaiproject.test;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Convenience base class for tests that need a kernel in a generic application context. Extend this class and add a
 * {@link ContextConfiguration} annotation with the locations (of XML bean files) or classes (Java configuration) set.
 *
 * If you need a web application context for MockMvc/WebClient, extend {@link ModiWebTest}.
 */
@ContextHierarchy({
        @ContextConfiguration(
                name = "kernel",
                loader = ModiContextLoader.class,
                initializers = ModiInitializer.class,
                locations = {
                        "classpath:org/sakaiproject/config/modi-configuration.xml",
                        "classpath:org/sakaiproject/kernel/components.xml"
                })
        })
@DirtiesContext
public abstract class ModiTest {
    static int igniteIndex = 0;

    /**
     * We set a dynamic property to increment an index for each integration test subclass.
     * Currently, there is enough state dependence within the wiring of the beans and the
     * infrastructure like Ignite and Hibernate that the entire kernel must be recreated,
     * so the integration test boundary serves as the kernel lifetime boundary.
     */
    @DynamicPropertySource
    static void igniteIndexProperty(DynamicPropertyRegistry registry) {
        registry.add("ignite.instance.index", () -> ++igniteIndex);
    }
}
