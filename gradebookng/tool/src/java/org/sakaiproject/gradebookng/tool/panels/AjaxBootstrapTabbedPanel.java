package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;

import java.util.List;

public class AjaxBootstrapTabbedPanel<T extends ITab> extends AjaxTabbedPanel<T> {

	public AjaxBootstrapTabbedPanel(String id, List<T> tabs) {
		super(id, tabs);
	}

	public AjaxBootstrapTabbedPanel(String id, List<T> tabs, IModel<Integer> model) {
		super(id, tabs, model);
	}

	@Override
	protected String getSelectedTabCssClass() {
		return "active";
	}

	@Override
	protected String getTabContainerCssClass() {
		return "nav nav-tabs";
	}
}