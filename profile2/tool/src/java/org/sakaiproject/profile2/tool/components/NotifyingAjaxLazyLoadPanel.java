package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * A Sakai-specific extension to the built in AjaxLazyLoadPanel that allows javascript to be run after its loaded.
 * 
 * <p>e.g. response.renderOnDomReadyJavascript("setMainFrameHeight(window.name);");</p>
 * 
 * @author Steve Swinsburg steve.swinsburg@gmail.com)
 *
 */
public abstract class NotifyingAjaxLazyLoadPanel extends AjaxLazyLoadPanel implements IHeaderContributor {

	
	private static final long serialVersionUID = 1L;

	public NotifyingAjaxLazyLoadPanel(String id) {
		super(id);
	}

	public abstract void renderHead(IHeaderResponse response);
	
	public abstract Component getLazyLoadComponent(String markupId);
}