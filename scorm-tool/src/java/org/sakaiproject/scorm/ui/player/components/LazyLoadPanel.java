package org.sakaiproject.scorm.ui.player.components;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;

public abstract class LazyLoadPanel extends Panel {
	
	public LazyLoadPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		Component loadingComponent = getLoadingComponent("content");
		add(loadingComponent.setRenderBodyOnly(true));
		
		loadingComponent.add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			protected void respond(AjaxRequestTarget target)
			{
				Component component = getLazyLoadComponent("content", target);
				component.setOutputMarkupId(true);
				LazyLoadPanel.this.replace(component.setRenderBodyOnly(true));
				target.addComponent(LazyLoadPanel.this);
			}
		
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				response.renderOnDomReadyJavascript(getCallbackScript().toString());
			}
			
			// Ignore the onlyTargetActivePage param
			protected CharSequence getCallbackScript(boolean onlyTargetActivePage)
			{
				return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "'");
			}
			
			public CharSequence getCallbackUrl()
			{
				if (getComponent() == null)
				{
					throw new IllegalArgumentException(
							"Behavior must be bound to a component to create the URL");
				}
				
				final RequestListenerInterface rli;
				
				rli = IBehaviorListener.INTERFACE;
				
				WebRequest webRequest = (WebRequest)getComponent().getRequest();
				HttpServletRequest servletRequest = webRequest.getHttpServletRequest();

				String toolUrl = servletRequest.getContextPath();
				
				AppendingStringBuffer url = new AppendingStringBuffer();
				url.append(toolUrl).append("/");
				url.append(getComponent().urlFor(this, rli));

				return url;
			}
		});
	}

	/**
	 * @param markupId The components markupid.
	 * @return The component that must be lazy created.
	 */
	public abstract Component getLazyLoadComponent(String markupId, AjaxRequestTarget target);
	
	/**
	 * @param markupId The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId)
	{
		return new Label(markupId,"<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>").setEscapeModelStrings(false);
	}
	
	
}
