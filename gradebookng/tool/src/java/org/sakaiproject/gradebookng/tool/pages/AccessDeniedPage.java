package org.sakaiproject.gradebookng.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Access denied page.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AccessDeniedPage extends BasePage {

	private static final long serialVersionUID = 1L;

	private String message;
	
	public AccessDeniedPage(PageParameters params) {
		this.message = params.get("message").toString();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		add(new Label("message", this.message));
	}

	
}
