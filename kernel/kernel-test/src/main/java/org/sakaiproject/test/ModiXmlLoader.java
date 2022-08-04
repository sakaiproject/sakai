package org.sakaiproject.test;

import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.support.GenericXmlContextLoader;

/**
 * Create a loader for XML file-based configuration. Helps supply the same behavior as the regular smart loader,
 * while registering our new context as the global one (so the ComponentManager cover works).
 */
public class ModiXmlLoader extends GenericXmlContextLoader {
    @Override
    protected void prepareContext(GenericApplicationContext context) {
        super.prepareContext(context);
        GlobalApplicationContext.setContext(context);
    }
}
