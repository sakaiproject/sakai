package org.sakaiproject.gradebookng.framework;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.sakaiproject.util.ResourceLoader;

/**
 * IStringResourceLoader that plugs into sakai's resource loader.
 */
public class GradebookNgStringResourceLoader implements IStringResourceLoader {

    private ResourceLoader loader = new ResourceLoader("org.sakaiproject.gradebookng.GradebookNgApplication");

    @Override
    public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation) {
        return loadString(locale, key);
    }

    @Override
    public String loadStringResource(Component component, String key, Locale locale, String style, String variation) {
        if (loader.containsKey(key)) {
            return loadString(locale, key);
        }
        // May contain the component prefix that should be removed
        key = key.replaceFirst(component.getId() + ".", "");
        return loadString(locale, key);
    }

    private String loadString(Locale locale, String key) {
        Locale sakaiLocale = loader.getLocale();
        if (locale != null && sakaiLocale == null) {
            loader.setContextLocale(locale);
        }
        return loader.getString(key);
    }
}
