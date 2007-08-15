package org.sakaiproject.scorm.tool.pages;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;

public class LaunchFrameset extends WebPage {
	private static final long serialVersionUID = 1L;
	protected final FrameTarget frameTarget = new FrameTarget(BlankPage.class);
	private WebComponent navFrameTag;
	public WebComponent contentFrameTag;
	
	public final class FrameModel extends Model
	{
		private static final long serialVersionUID = 1L;

		public Object getObject()
		{
			RequestCycle cycle = getRequestCycle();
			IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			
			return encoder.encode(cycle, new BookmarkablePageRequestTarget("contentFrame",
					frameTarget.getFrameClass(), new PageParameters()));
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
			
		//add(new ApiPanel("api-panel"));
		//NavigationFrame navFrame = new NavigationFrame(this, pageParams);
		
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		CharSequence navFrameSrc = //RequestCycle.get().urlFor(navFrame);
			encoder.encode(cycle, new BookmarkablePageRequestTarget("navFrame", NavigationFrame.class, pageParams));
		navFrameTag = new WebComponent("navFrame");
		navFrameTag.setOutputMarkupId(true);
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
	
	/*public void renderHead(IHeaderResponse response) {
		StringBuffer js = new StringBuffer();
		
		//js.append("var API_1484_11 = null;\n");
		//js.append("function initAPI() {   ")
		js.append("API_1484_11 = window.navFrame")
			//.append(navFrameTag.getMarkupId())
			.append(".document.APIAdapter; \n");
		
		response.renderJavascript("var API_1484_11 = null;\n", "api");
		response.renderOnLoadJavascript(js.toString());
	}*/
	
	public FrameTarget getFrameTarget()
	{
		return frameTarget;
	}
	
	public boolean isVersioned()
	{
		return false;
	}
		
}
