package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

public class BreadcrumbPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final RepeatingView breadcrumbList;
	private int numberOfCrumbs;
	
	public BreadcrumbPanel(String id) {
		super(id);
		add(breadcrumbList = new RepeatingView("breadcrumbList"));
	}

	public void addBreadcrumb(IModel model, Class<?> pageClass, PageParameters params, boolean isEnabled) {
		BookmarkablePageLabeledLink link = new BookmarkablePageLabeledLink("breadcrumb", model, pageClass, params);
		link.setEnabled(isEnabled);
		
		Label separator = new Label("separator", new Model(">"));
		separator.setVisible(numberOfCrumbs > 0);
		
		WebMarkupContainer item = new WebMarkupContainer(breadcrumbList.newChildId());
		item.setRenderBodyOnly(true);
		item.add(separator);
		item.add(link);

		breadcrumbList.add(item);
		numberOfCrumbs++;
	}

	public int getNumberOfCrumbs() {
		return numberOfCrumbs;
	}
	
	
}
