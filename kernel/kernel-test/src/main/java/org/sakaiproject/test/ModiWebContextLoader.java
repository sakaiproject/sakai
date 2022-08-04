package org.sakaiproject.test;

import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.web.WebDelegatingSmartContextLoader;

/**
 * We go through all of this to be able to register the new context as the global one immediately after it's created. We
 * want the full smart/delegating behavior, but the Spring API doesn't quite let us plug in where we want. We can't
 * catch the context "on the way out", so we have to go this far to create it and register it.
 */
public class ModiWebContextLoader extends WebDelegatingSmartContextLoader {
    private final SmartContextLoader xmlLoader;
    private final SmartContextLoader annotationLoader;

    public ModiWebContextLoader() {
        xmlLoader = new ModiXmlWebLoader();
        annotationLoader = new ModiAnnotationWebLoader();
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
