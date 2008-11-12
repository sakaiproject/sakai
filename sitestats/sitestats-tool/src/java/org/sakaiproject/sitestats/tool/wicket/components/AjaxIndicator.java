package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;


public class AjaxIndicator extends WebMarkupContainer {
	private static final long	serialVersionUID	= 1L;

	public AjaxIndicator(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	/*
	 * (non-Javadoc)
	 * @see wicket.Component#onComponentTag( wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("src", "images/ajaxload.gif");
		tag.put("style", "display: none");
	}
}
