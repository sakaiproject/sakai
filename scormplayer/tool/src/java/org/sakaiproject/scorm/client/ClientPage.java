package org.sakaiproject.scorm.client;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.StringResourceModel;

public class ClientPage extends WebPage implements IHeaderContributor {
	protected static final ResourceReference HEADSCRIPTS = new CompressedResourceReference(ClientPage.class, "headscripts.js");
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";
	protected static final String TOOLBASE_CSS = "/library/skin/tool_base.css";
	protected static final String TOOL_CSS = "/library/skin/default/tool.css";
	
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(HEADSCRIPTS);
		response.renderCSSReference(TOOLBASE_CSS);
		response.renderCSSReference(TOOL_CSS);
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
	}
	
	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
}
