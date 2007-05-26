package org.sakaiproject.scorm.tool.pages;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.PageRequestTarget;

public class LaunchFrameset extends WebPage {
	private static final long serialVersionUID = 1L;
	private final FrameTarget frameTarget = new FrameTarget(ContentFrame.class);
	public WebComponent contentFrameTag;
	
	private final class FrameModel extends Model
	{
		private static final long serialVersionUID = 1L;

		public Object getObject()
		{
			RequestCycle cycle = getRequestCycle();
			IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			
			//if (frameTarget.getUrl() == null)
				return encoder.encode(cycle, new BookmarkablePageRequestTarget("contentFrame",
					frameTarget.getFrameClass(), new PageParameters()));
			//else
			//	return encoder.encode(cycle, new RedirectRequestTarget(frameTarget.getUrl()));
		}
	}
	
	public final class FrameTarget implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** the class of the bookmarkable page. */
		private Class frameClass;
		
		private String url = null;

		/**
		 * Construct.
		 */
		public FrameTarget()
		{
		}

		/**
		 * Construct.
		 * 
		 * @param frameClass
		 */
		public FrameTarget(Class frameClass)
		{
			this.frameClass = frameClass;
		}

		/**
		 * Gets frame class.
		 * 
		 * @return lefFrameClass
		 */
		public Class getFrameClass()
		{
			return frameClass;
		}

		/**
		 * Sets frame class.
		 * 
		 * @param frameClass
		 *            lefFrameClass
		 */
		public void setFrameClass(Class frameClass)
		{
			this.frameClass = frameClass;
		}
		
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}	
		
	}
	
	public LaunchFrameset(PageParameters pageParams) {	
		RequestCycle cycle = getRequestCycle();
				
		//NavigationFrame navFrame = new NavigationFrame(this, pageParams);
		
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		CharSequence navFrameSrc = //RequestCycle.get().urlFor(navFrame);
			encoder.encode(cycle, new BookmarkablePageRequestTarget("navFrame", NavigationFrame.class, pageParams));
		WebComponent navFrameTag = new WebComponent("navFrame");
		navFrameTag.add(new AttributeModifier("src", new Model((Serializable)navFrameSrc)));
		add(navFrameTag);

		//ContentFrame contentFrame = new ContentFrame(new PageParameters());
		
		contentFrameTag = new WebComponent("contentFrame");
		//CharSequence contentFrameSrc = RequestCycle.get().urlFor(PageMap.forName("contentFrame"), ContentFrame.class, new PageParameters());
		contentFrameTag.setOutputMarkupId(true);
		contentFrameTag.add(new AttributeModifier("src", new FrameModel()));
		//contentFrameTag.add(new AttributeModifier("src", new Model((Serializable)contentFrameSrc)));
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
