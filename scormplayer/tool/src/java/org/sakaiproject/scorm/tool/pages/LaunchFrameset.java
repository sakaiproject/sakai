package org.sakaiproject.scorm.tool.pages;

import java.io.Serializable;

import org.sakaiproject.scorm.tool.FrameTarget;
import org.sakaiproject.scorm.tool.pages.ContentFrame;
import org.sakaiproject.scorm.tool.pages.NavigationFrame;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.RequestCycle;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebPage;
import wicket.markup.html.pages.RedirectPage;
import wicket.model.Model;
import wicket.request.IRequestCodingStrategy;
import wicket.request.target.basic.RedirectRequestTarget;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.PageRequestTarget;

public class LaunchFrameset extends WebPage {
	private final FrameTarget frameTarget = new FrameTarget(ContentFrame.class);
	public WebComponent contentFrameTag;
	
	private final class FrameModel extends Model
	{
		public Object getObject(Component component)
		{
			RequestCycle cycle = getRequestCycle();
			IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			
			//if (frameTarget.getUrl() == null)
			return encoder.encode(cycle, new BookmarkablePageRequestTarget("contentFrame",
					frameTarget.getFrameClass()));
			//else
			//	return encoder.encode(cycle, new RedirectRequestTarget(frameTarget.getUrl()));
		}
	}
	
	public LaunchFrameset() {	
		RequestCycle cycle = getRequestCycle();

		NavigationFrame navFrame = new NavigationFrame(this);
		
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		
		CharSequence navFrameSrc = encoder.encode(cycle, new PageRequestTarget(navFrame));
		WebComponent navFrameTag = new WebComponent("navFrame");
		navFrameTag.add(new AttributeModifier("src", new Model((Serializable)navFrameSrc)));
		add(navFrameTag);

		contentFrameTag = new WebComponent("contentFrame");
		contentFrameTag.setOutputMarkupId(true);
		contentFrameTag.add(new AttributeModifier("src", new FrameModel()));
		add(contentFrameTag);
	}
	
	public void refreshContent(AjaxRequestTarget target) {
		//setResponsePage(new RedirectPage("http://www.upenn.edu"));
		//target.addComponent(contentFrameTag);
	}
	
	public FrameTarget getFrameTarget()
	{
		return frameTarget;
	}
	
	public boolean isVersioned()
	{
		return false;
	}
		
}
