package org.sakaiproject.scorm.client.pages;

import java.io.Serializable;

import org.sakaiproject.scorm.client.ScormTool;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.RequestCycle;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebPage;
import wicket.model.Model;
import wicket.request.IRequestCodingStrategy;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.PageRequestTarget;
import wicket.util.lang.PackageName;

public class LaunchFrameset extends WebPage {
	private final FrameTarget frameTarget = new FrameTarget(ContentFrame.class);

	private final class FrameModel extends Model
	{
		public Object getObject(Component component)
		{
			RequestCycle cycle = getRequestCycle();
			IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			return encoder.encode(cycle, new BookmarkablePageRequestTarget("contentFrame",
					frameTarget.getFrameClass()));
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

		WebComponent contentFrameTag = new WebComponent("contentFrame");
		contentFrameTag.add(new AttributeModifier("src", new FrameModel()));
		add(contentFrameTag);
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
