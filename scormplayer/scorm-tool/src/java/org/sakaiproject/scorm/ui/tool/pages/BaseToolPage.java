package org.sakaiproject.scorm.ui.tool.pages;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;

public class BaseToolPage extends WebPage implements IHeaderContributor {
	private static final long serialVersionUID = 1L;
	private static final ResourceReference SCORM_CSS = new CompressedResourceReference(BaseToolPage.class, "res/scorm.css");
	//private static final ResourceReference JQUERY_JS = new CompressedResourceReference(BaseToolPage.class, "res/jquery-latest.pack.js");
	//private static final ResourceReference THICKBOX_JS = new CompressedResourceReference(BaseToolPage.class, "res/thickbox-compressed.js");
	//private static final ResourceReference THICKBOX_CSS = new CompressedResourceReference(BaseToolPage.class, "res/thickbox.css");
	
	protected static final String TOOLBASE_CSS = "/library/skin/tool_base.css";
	protected static final String TOOL_CSS = "/library/skin/default/tool.css";
	
	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(TOOLBASE_CSS);
		response.renderCSSReference(TOOL_CSS);
		response.renderCSSReference(SCORM_CSS);
		//response.renderCSSReference(THICKBOX_CSS);
		//response.renderJavascriptReference(JQUERY_JS);
		//response.renderJavascriptReference(THICKBOX_JS);
	}

}
