package org.sakaiproject.sitestats.tool.wicket.pages;

import java.awt.image.BufferedImage;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;

/**
 * @author Nuno Fernandes
 */
public class MaximizedImagePage extends BasePage {
	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade 	facade;
	
	private BufferedImage 			bufferedImage;
	private Class					returnPage;
	private IModel					backButtonMessageModel;
	
	
	public MaximizedImagePage(final BufferedImage bufferedImage, final Class returnPage) {
		this(bufferedImage, returnPage, new ResourceModel("overview_back"));
	}
	
	public MaximizedImagePage(final BufferedImage bufferedImage, final Class returnPage, IModel backButtonMessageModel) {
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			this.bufferedImage = bufferedImage;
			this.returnPage = returnPage;
			this.backButtonMessageModel = backButtonMessageModel;
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}
	
	@SuppressWarnings("serial")
	private void renderBody() {
		NonCachingImage image = new NonCachingImage("image") {
			@SuppressWarnings("serial")
			@Override
			protected Resource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData() {
						return toImageData(getBufferedImage());
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
				setResponsePage(returnPage);
				super.onSubmit();
			}
		};
		back.setModel(backButtonMessageModel);
		back.setDefaultFormProcessing(true);
		form.add(back);
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}
}

