package org.sakaiproject.sitestats.tool.wicket.components;

import java.awt.image.BufferedImage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.sakaiproject.sitestats.tool.wicket.pages.MaximizedImagePage;

@SuppressWarnings("serial")
public abstract class AjaxLazyLoadImage extends Panel {
	private Link						link								= null;
	private WebMarkupContainer			js									= null;
	private Class						returnPage							= null;
	private AbstractDefaultAjaxBehavior ajaxBehavior						= null;

	public AjaxLazyLoadImage(final String id, final BufferedImage bufferedImage, final Class returnPage) {
		this(id, bufferedImage, returnPage, new ResourceModel("overview_back"));
	}
	
	public AjaxLazyLoadImage(final String id, final BufferedImage bufferedImage, final Class returnPage, final IModel backButtonMessageModel) {
		super(id);
		setOutputMarkupId(true);
		this.returnPage = returnPage;
		final Component loadingComponent = getLoadingComponent("content");
		
		link = createMaximizedLink("link", backButtonMessageModel);
		link.setOutputMarkupId(true);
		link.setEnabled(false);
		link.add(loadingComponent.setRenderBodyOnly(true));
		add(link);
		
		ajaxBehavior = new AbstractDefaultAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				renderImage(target);
				target.addComponent(link);				
			}	
		};
		add(ajaxBehavior);
	}
	
	public CharSequence getCallbackUrl() {
		return ajaxBehavior.getCallbackUrl();
	}
	
	public Image renderImage(AjaxRequestTarget target) {
		link.setEnabled(true);
		link.removeAll();
		Image img = createImage("content", getBufferedImage());
		link.add(img);
		if(target != null) {
			target.appendJavascript("jQuery('#"+img.getMarkupId(true)+"').fadeIn();");
		}
		return img;
	}

	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId) {
		Label indicator = new Label(markupId, "<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>");
		indicator.setEscapeModelStrings(false);
		indicator.add(new AttributeModifier("title", true, new Model("...")));
		return indicator;
	}

	public abstract BufferedImage getBufferedImage();

	public abstract BufferedImage getBufferedMaximizedImage();

	private Link createMaximizedLink(final String id, final IModel backButtonMessageModel) {
		Link link = new Link(id) {
			@Override
			public void onClick() {
				setResponsePage(new MaximizedImagePage(getBufferedMaximizedImage(), returnPage, backButtonMessageModel));
			}			
		};
		link.setOutputMarkupId(true);
		return link;
	}
	
	private Image createImage(final String id, final BufferedImage bufferedImage) {
		NonCachingImage chartImage = new NonCachingImage(id) {
			@Override
			protected Resource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData() {
						return toImageData(bufferedImage);
					}

					@Override
					protected void setHeaders(WebResponse response) {
						response.setHeader("Pragma", "no-cache");
						response.setHeader("Cache-Control", "no-cache");
						response.setDateHeader("Expires", 0);
						response.setContentType("image/png");
						response.setContentLength(getImageData().length);
						response.setAjax(true);
					}
				}.setCacheable(false);
			}
		};
		chartImage.setOutputMarkupId(true);
		return chartImage;
	}
}
