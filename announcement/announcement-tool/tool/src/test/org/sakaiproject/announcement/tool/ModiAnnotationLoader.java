package org.sakaiproject.announcement.tool;

import org.jetbrains.annotations.NotNull;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

public class ModiAnnotationLoader extends AnnotationConfigContextLoader {
    @Override
    protected @NotNull GenericApplicationContext createContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        GlobalApplicationContext.setContext(context);
        return context;
    }
}
