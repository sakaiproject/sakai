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
package org.sakaiproject.sitestats.tool.wicket.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.sitestats.tool.wicket.pages.MaximizedImagePage;

@Slf4j
public abstract class AjaxLazyLoadImage extends Panel {
	private static final long			serialVersionUID					= 1L;
	private SubmitLink					link								= null;
	private Page						returnPage							= null;
	private Class<?>					returnClass							= null;
	private AbstractDefaultAjaxBehavior chartRenderAjaxBehavior				= null;
	
	private Form						form								= null;
	private boolean						autoDetermineChartSizeByAjax 		= false;
	private int							selectedWidth						= 400;
	private int							selectedHeight						= 200;
	private int							maxWidth							= 800;
	private int							maxHeight							= 600;

	// State:
	// 0:add loading component
	// 1:loading component added, waiting for ajax replace
	// 2:ajax replacement completed
	private byte state = 0;
	
	public AjaxLazyLoadImage(final String id, final Class<?> returnClass) {
		super(id);
		this.returnClass = returnClass;
		init();
	}
	
	public AjaxLazyLoadImage(final String id, final Page returnPage) {
		super(id);
		this.returnPage = returnPage;
		init();
	}
	private void init() {
		setOutputMarkupId(true);	
		
		// render chart by ajax, uppon request
		chartRenderAjaxBehavior = new AbstractDefaultAjaxBehavior() {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				//log.debug("chartRenderAjaxBehavior.Responding for "+ getId());
				renderImage(target, true);
			}
			
			@Override
			public boolean isEnabled(Component component) {
				return state < 2;
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				
				attributes.setChannel(new AjaxChannel(getId()));
			}
		};
		add(chartRenderAjaxBehavior);
		
		// fields for maximized chart size
		setDefaultModel(new CompoundPropertyModel(this));
		form = new Form("chartForm");
		form.add(new HiddenField("maxWidth"));
		form.add(new HiddenField("maxHeight"));
		add(form);
	}
	
	@Override
	protected void onBeforeRender() {
		if(state == 0){
			final Component loadingComponent = getLoadingComponent("content");		
			link = createMaximizedLink("link");
			link.setOutputMarkupId(true);
			link.setEnabled(false);
			link.add(loadingComponent.setRenderBodyOnly(true));
			add(link);
			setState((byte) 1);
		}else if(state == 2){
			final Component loadingComponent = getLoadingComponent("content");
			link.setEnabled(false);
			link.removeAll();
			link.add(loadingComponent.setRenderBodyOnly(true));
			add(link);
			setState((byte) 1);
		}
		super.onBeforeRender();
	}
	
	public CharSequence getCallbackUrl() {
		return chartRenderAjaxBehavior.getCallbackUrl();
	}
	
	public Image renderImage(AjaxRequestTarget target, boolean fullRender) {
		if(returnPage != null || returnClass != null) {
			link.add(new AttributeModifier("title", new ResourceModel("click_to_max")));
			link.setEnabled(true);
		}
		link.removeAll();
		Image img = null;
		if(!autoDetermineChartSizeByAjax) {
			img = createImage("content", getImageData());
		}else{
			img = createImage("content", getImageData(selectedWidth, selectedHeight));
		}
		img.add(AttributeModifier.replace("style", "display: none; margin: 0 auto;"));
		link.add(img);
		setState((byte) 1);
		if(fullRender) {
			if(target != null) {
				target.add(link);	
				target.appendJavaScript("jQuery('#"+img.getMarkupId()+"').fadeIn();");
			}		
			setState((byte) 2);
		}
		return img;
	}

	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId) {
		Label indicator = new Label(markupId, "<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR, null) + "\"/>");
		indicator.setEscapeModelStrings(false);
		indicator.add(new AttributeModifier("title", new Model("...")));
		return indicator;
	}

	public abstract byte[] getImageData();

	public abstract byte[] getImageData(int width, int height);

	private SubmitLink createMaximizedLink(final String id) {
		SubmitLink link = new SubmitLink(id, form) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public void onSubmit() {
				if(returnPage != null || returnClass != null) {
					setResponsePage(new MaximizedImagePage(returnPage, returnClass) {
						@Override
						public byte[] getMaximizedImageData() {
							int _width = (int) ((int) maxWidth * 0.98);
							return AjaxLazyLoadImage.this.getImageData(_width, 2 * _width / 3);
						}						
					});
				}
				super.onSubmit();
			}	
		};
		link.setOutputMarkupId(true);
		return link;
	}
	
	private Image createImage(final String id, final byte[] imageData) {
		NonCachingImage chartImage = new NonCachingImage(id) {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected IResource getImageResource() {
				return new DynamicImageResource() {
					private static final long	serialVersionUID	= 1L;

					@Override
					protected byte[] getImageData(IResource.Attributes attributes) {
						return imageData;
					}

					// adapted from https://cwiki.apache.org/confluence/display/WICKET/JFreeChart+and+wicket+example
					@Override
					protected void configureResponse(AbstractResource.ResourceResponse response, IResource.Attributes attributes)
					{
						super.configureResponse(response, attributes);
						
						response.setCacheDuration(Duration.NONE);
						response.setCacheScope(CacheScope.PRIVATE);
					}
				};
			}
		};
		chartImage.setOutputMarkupId(true);
		chartImage.setOutputMarkupPlaceholderTag(true);
		return chartImage;
	}
	
	public void setAutoDetermineChartSizeByAjax(final String jquerySelectorForContainer) {
		autoDetermineChartSizeByAjax = true;
		AbstractDefaultAjaxBehavior determineChartSizeBehavior = new AbstractDefaultAjaxBehavior() {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				// parse desired image size
				Request req = RequestCycle.get().getRequest();

				selectedWidth = req.getQueryParameters().getParameterValue("width").toInt(400);					

				selectedHeight = req.getQueryParameters().getParameterValue("height").toInt(200);
				if(selectedHeight < 200)
				{
					selectedHeight = 200;
				}
				// render chart image
				renderImage(target, true);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				
				super.renderHead(component, response);
				response.render(OnDomReadyHeaderItem.forScript(getCallbackScript(component)));
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				
				attributes.setChannel(new AjaxChannel(getId()));
				
				String dynamicExtraParams = "return { 'height': jQuery('" + jquerySelectorForContainer
						+ "').height(), 'width': jQuery('" + jquerySelectorForContainer + "').width() }";
				attributes.getDynamicExtraParameters().add(dynamicExtraParams);
			}
		};	
		
		add(determineChartSizeBehavior);
	}
	
	public int getMaxWidth() {
		return maxWidth;
	}
	
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	
	public int getMaxHeight() {
		return maxHeight;
	}
	
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	private void setState(byte state) {
		if(this.state != state){
			addStateChange();
		}
		this.state = state;
	}
	
}
