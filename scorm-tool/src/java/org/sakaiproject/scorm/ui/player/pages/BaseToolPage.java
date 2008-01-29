package org.sakaiproject.scorm.ui.player.pages;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;

public class BaseToolPage extends WebPage implements IHeaderContributor {
	private static final long serialVersionUID = 1L;
	private static final ResourceReference SCORM_CSS = new CompressedResourceReference(BaseToolPage.class, "res/scorm.css");

	protected static final String TOOLBASE_CSS = "/library/skin/tool_base.css";
	protected static final String TOOL_CSS = "/library/skin/default/tool.css";
	
	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(TOOLBASE_CSS);
		response.renderCSSReference(TOOL_CSS);
		response.renderCSSReference(SCORM_CSS);
	}

}
