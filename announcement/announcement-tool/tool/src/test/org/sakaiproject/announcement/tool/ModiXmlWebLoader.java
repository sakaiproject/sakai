package org.sakaiproject.announcement.tool;

import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.GenericXmlWebContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Create a loader for XML file-based configuration. Helps supply the same behavior as the regular smart loader,
 * while registering our new web context as the global one (so the ComponentManager cover works).
 */
public class ModiXmlWebLoader extends GenericXmlWebContextLoader {
    @Override
    protected void prepareContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        super.prepareContext(context, mergedConfig);
        GlobalApplicationContext.setContext(context);
    }
}
