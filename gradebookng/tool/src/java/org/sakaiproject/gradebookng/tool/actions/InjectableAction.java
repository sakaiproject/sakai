package org.sakaiproject.gradebookng.tool.actions;

import org.apache.wicket.injection.Injector;

abstract public class InjectableAction implements Action {
	public InjectableAction() {
		// inject Spring dependencies into this class and derived instances
		Injector.get().inject(this);
	}
}
