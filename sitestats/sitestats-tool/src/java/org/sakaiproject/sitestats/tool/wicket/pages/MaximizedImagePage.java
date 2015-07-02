/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.Page;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.time.Duration;

import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * @author Nuno Fernandes
 */
public abstract class MaximizedImagePage extends BasePage {
	private static final long		serialVersionUID	= 1L;
	
	private Page					returnPage;
	private Class					returnClass;
	
	
	public MaximizedImagePage() {
		init(null, null);
	}
	
	public MaximizedImagePage(final Page returnPage, final Class returnClass) {
		init(returnPage, returnClass);
	}
	
	private void init(final Page returnPage, final Class returnClass) {
		String siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		boolean allowed = Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId);
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
		response.render(JavaScriptHeaderItem.forUrl(JQUERYSCRIPT));
		response.render(OnDomReadyHeaderItem.forScript("setMainFrameHeightNoScroll(window.name, 750);"));
	}
	
	public abstract byte[] getMaximizedImageData();
	
	@SuppressWarnings("serial")
	private void renderBody() {
		NonCachingImage image = new NonCachingImage("image") {
			@SuppressWarnings("serial")
			@Override
			protected IResource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData(IResource.Attributes attributes) {
						return getMaximizedImageData();
					}

					// adapted from https://cwiki.apache.org/confluence/display/WICKET/JFreeChart+and+wicket+example
					@Override
					protected void configureResponse(AbstractResource.ResourceResponse response, IResource.Attributes attributes)
					{
						super.configureResponse(response, attributes);
						
						response.setCacheDuration(Duration.NONE);
						response.setCacheScope(WebResponse.CacheScope.PRIVATE);
					}
				};
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

