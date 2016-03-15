package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;

public class AjaxBootstrapTabbedPanel<T extends ITab> extends AjaxTabbedPanel<T> {

	private static final long serialVersionUID = 1L;

	public AjaxBootstrapTabbedPanel(final String id, final List<T> tabs) {
		super(id, tabs);
	}

	public AjaxBootstrapTabbedPanel(final String id, final List<T> tabs, final IModel<Integer> model) {
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