package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

public class AttemptNumberPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final RepeatingView attemptNumberLinks;
	
	private long attemptNumber;
	
	public AttemptNumberPanel(String id, long numberOfAttempts, Class<?> pageClass, PageParameters pageParams) {
		super(id);

		this.attemptNumberLinks = new RepeatingView("attemptNumberLinks");
		add(attemptNumberLinks);
		
		attemptNumber = -1;
		
		for (long i=1;i<=numberOfAttempts;i++) {
			this.addAttemptNumberLink(i, pageClass, pageParams, attemptNumberLinks, attemptNumber);
		}
	}

	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	protected void addAttemptNumberLink(long i, Class<?> pageClass, PageParameters params, RepeatingView container, long current)
	{
		params.put("attemptNumber", i);
		
		BookmarkablePageLabeledLink link = new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), pageClass, params);

		if (i == current) {
			link.setEnabled(false);
		}
			
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(link);

		container.add(item);
	}


	public long getAttemptNumber() {
		return attemptNumber;
	}


	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}
}
