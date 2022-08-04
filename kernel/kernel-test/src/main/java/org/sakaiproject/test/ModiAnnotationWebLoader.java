package org.sakaiproject.test;

import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;

/**
 * Create a loader for annotation-based configuration. Helps supply the same behavior as the regular smart loader,
 * while registering our new context as the global one (so the ComponentManager cover works).
 */
public class ModiAnnotationWebLoader extends AnnotationConfigWebContextLoader {
    @Override
    protected void prepareContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        super.prepareContext(context, mergedConfig);
        GlobalApplicationContext.setContext(context);
    }
}
