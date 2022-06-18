package org.sakaiproject.announcement.tool;

import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.support.DelegatingSmartContextLoader;

/**
 * We go through all of this to be able to register the new context as the global one immediately after it's created. We
 * want the full smart/delegating behavior, but the Spring API doesn't quite let us plug in where we want. We can't
 * catch the context "on the way out", so we have to go this far to create it and register it.
 */
public class ModiContextLoader extends DelegatingSmartContextLoader {
    private final SmartContextLoader xmlLoader;
    private final SmartContextLoader annotationLoader;

    public ModiContextLoader() {
        xmlLoader = new ModiXmlLoader();
        annotationLoader = new ModiAnnotationLoader();
    }

    @Override
    public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) super.loadContext(mergedConfig);
        context.start();
        return context;
    }

    @Override
    protected SmartContextLoader getXmlLoader() {
        return xmlLoader;
    }

    @Override
    protected SmartContextLoader getAnnotationConfigLoader() {
        return annotationLoader;
    }
}
