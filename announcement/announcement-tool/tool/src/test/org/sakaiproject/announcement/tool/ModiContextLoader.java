package org.sakaiproject.announcement.tool;

import org.jetbrains.annotations.NotNull;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.support.AbstractDelegatingSmartContextLoader;

/**
 * We go through all of this to be able to register the new context as the global one immediately after it's created. We
 * want the full smart/delegating behavior, but the Spring API doesn't quite let us plug in where we want. We can't
 * catch the context "on the way out", so we have to go this far to create it and register it.
 */
public class ModiContextLoader extends AbstractDelegatingSmartContextLoader {
    private final SmartContextLoader xmlLoader;
    private final SmartContextLoader annotationLoader;

    public ModiContextLoader() {
        xmlLoader = new ModiXmlLoader();
        annotationLoader = new ModiAnnotationLoader();
    }

    @Override
    protected @NotNull SmartContextLoader getXmlLoader() {
        return xmlLoader;
    }

    @Override
    protected @NotNull SmartContextLoader getAnnotationConfigLoader() {
        return annotationLoader;
    }
}
