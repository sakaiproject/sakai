package org.sakaiproject.announcement.tool;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(
        loader = ModiContextLoader.class,
        initializers = ModiInitializer.class,
        locations = {
            "classpath:org/sakaiproject/config/sakai-configuration.xml",
            "classpath:org/sakaiproject/kernel/components.xml",
            "classpath:org/sakaiproject/config/test-configuration.xml"
        })
public abstract class ModiTest {
}
