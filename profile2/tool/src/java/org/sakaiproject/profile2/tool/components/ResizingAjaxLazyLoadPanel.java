package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * A Sakai-specific extension to the built in AjaxLazyLoadPanel that resizes the portlet iframe
 * after the lazy loaded panel has been fully rendered.
 * 
 * @author Steve Swinsburg steve.swinsburg@gmail.com)
 *
 */
public abstract class ResizingAjaxLazyLoadPanel extends AjaxLazyLoadPanel implements IHeaderContributor {

	
	private static final long serialVersionUID = 1L;

	public ResizingAjaxLazyLoadPanel(String id) {
		super(id);
	}

	public void renderHead(IHeaderResponse response){
		//why won't this resize the iframe base on its contents?
		response.renderOnDomReadyJavascript("setMainFrameHeight(window.name);");
	}

	public abstract Component getLazyLoadComponent(String markupId);
}