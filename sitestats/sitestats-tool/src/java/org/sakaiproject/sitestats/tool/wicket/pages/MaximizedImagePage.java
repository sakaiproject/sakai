package org.sakaiproject.sitestats.tool.wicket.pages;

import java.awt.image.BufferedImage;

import org.apache.wicket.Page;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;

/**
 * @author Nuno Fernandes
 */
public abstract class MaximizedImagePage extends BasePage {
	private static final long		serialVersionUID	= 1L;
	
	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade 	facade;
	
	private Page					returnPage;
	private Class					returnClass;
	
	
	public MaximizedImagePage() {
		init(null, null);
	}
	
	public MaximizedImagePage(final Page returnPage, final Class returnClass) {
		init(returnPage, returnClass);
	}
	
	private void init(final Page returnPage, final Class returnClass) {
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			this.returnPage = returnPage;
			this.returnClass = returnClass;
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderJavascriptReference("/library/js/jquery.js");
		response.renderOnDomReadyJavascript("setMainFrameHeightNoScroll(window.name, 750);");
	}
	
	public abstract BufferedImage getBufferedMaximizedImage();
	
	@SuppressWarnings("serial")
	private void renderBody() {
		NonCachingImage image = new NonCachingImage("image") {
			@SuppressWarnings("serial")
			@Override
			protected Resource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData() {
						return toImageData(getBufferedMaximizedImage());
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
		add(image);
		
		Form form = new Form("form");
		add(form);
		
		Button back = new Button("back") {
			@Override
			public void onSubmit() {
				if(returnPage != null) {
					setResponsePage(returnPage);
				}else if(returnClass != null) {
					setResponsePage(returnClass);
				}
				super.onSubmit();
			}
		};
		back.setDefaultFormProcessing(true);
		form.add(back);
	}
}

