package org.sakaiproject.test;

import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Create a loader for annotation-based configuration. Helps supply the same behavior as the regular smart loader,
 * while registering our new context as the global one (so the ComponentManager cover works).
 */
public class ModiAnnotationLoader extends AnnotationConfigContextLoader {
    @Override
    protected void prepareContext(GenericApplicationContext context) {
        super.prepareContext(context);
        GlobalApplicationContext.setContext(context);
    }
}
