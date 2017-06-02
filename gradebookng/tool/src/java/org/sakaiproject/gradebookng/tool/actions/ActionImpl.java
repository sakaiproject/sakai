package org.sakaiproject.gradebookng.tool.actions;

import org.apache.wicket.injection.Injector;

abstract public class ActionImpl  implements Action {
    public ActionImpl() {
        //inject Spring dependencies into this class and derived instances
        Injector.get().inject(this);
    }
}
