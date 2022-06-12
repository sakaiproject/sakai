package org.sakaiproject.announcement.tool;

import org.jetbrains.annotations.NotNull;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.support.GenericXmlContextLoader;

public class ModiXmlLoader extends GenericXmlContextLoader {
    @Override
    protected @NotNull GenericApplicationContext createContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        GlobalApplicationContext.setContext(context);
        return context;
    }
}
