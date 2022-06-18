package org.sakaiproject.announcement.tool;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(
        loader = ModiContextLoader.class,
        initializers = ModiInitializer.class,
        locations = {
            "classpath:org/sakaiproject/config/modi-configuration.xml",
            "classpath:org/sakaiproject/kernel/components.xml"
        })
public abstract class ModiTest {
}
