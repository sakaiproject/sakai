package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.version.undo.Change;
import org.sakaiproject.sitestats.tool.wicket.pages.MaximizedImagePage;

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
				//System.out.println("chartRenderAjaxBehavior.Responding for "+ getId());
				renderImage(target, true);
			}
			
			@Override
			public boolean isEnabled(Component component) {
				return state < 2;
			}
			
			@Override
			protected String getChannelName() {
				return getId();
			}
		};
		add(chartRenderAjaxBehavior);
		
		// fields for maximized chart size
		setModel(new CompoundPropertyModel(this));
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
		return chartRenderAjaxBehavior.getCallbackUrl(false);
	}
	
	public Image renderImage(AjaxRequestTarget target, boolean fullRender) {
		if(returnPage != null || returnClass != null) {
			link.add(new AttributeModifier("title", true, new ResourceModel("click_to_max")));
			link.setEnabled(true);
		}
		link.removeAll();
		Image img = null;
		if(!autoDetermineChartSizeByAjax) {
			img = createImage("content", getImageData());
		}else{
			img = createImage("content", getImageData(selectedWidth, selectedHeight));
		}
		img.add(new SimpleAttributeModifier("style", "display: none; margin: 0 auto;"));
		link.add(img);
		setState((byte) 1);
		if(fullRender) {
			if(target != null) {
				target.addComponent(link);	
				target.appendJavascript("jQuery('#"+img.getMarkupId()+"').fadeIn();");
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
		Label indicator = new Label(markupId, "<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>");
		indicator.setEscapeModelStrings(false);
		indicator.add(new AttributeModifier("title", true, new Model("...")));
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
			protected Resource getImageResource() {
				return new DynamicImageResource() {
					private static final long	serialVersionUID	= 1L;

					@Override
					protected byte[] getImageData() {
						return imageData;
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
				try{
					selectedWidth = (int) Float.parseFloat(req.getParameter("width"));					
				}catch(NumberFormatException e){
					e.printStackTrace();
					selectedWidth = 400;
				}
				try{
					selectedHeight = (int) Float.parseFloat(req.getParameter("height"));
					if(selectedHeight < 200) {
						selectedHeight = 200;
					}
				}catch(NumberFormatException e){
					e.printStackTrace();
					selectedHeight = 200;
				}
				
				// render chart image
				renderImage(target, true);
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				response.renderOnDomReadyJavascript(getScript(null, getScript(null, null)));
				super.renderHead(response);
			}
			
			private String getScript(String onSuccess, String onFailure) {
				StringBuilder buff = new StringBuilder();
				buff.append("wicketAjaxGet('");
				buff.append(getCallbackUrl(false));
				buff.append("&width='+ jQuery('"+jquerySelectorForContainer+"').width()+'");
				buff.append("&height='+ jQuery('"+jquerySelectorForContainer+"').height()");
				buff.append(",function() {");
				if(onSuccess !=  null) {
					buff.append(onSuccess);
				}
				buff.append("}, function() {");
				if(onFailure !=  null) {
					buff.append(onFailure);
				}
				buff.append("}");
				buff.append(",null, '" + getChannelName() + "'");
				buff.append(")");
				return buff.toString();
			}
			
			@Override
			protected String getChannelName() {
				return getId();
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
			addStateChange(new StateChange(this.state));
		}
		this.state = state;
	}
	
	private final class StateChange extends Change {
		private static final long	serialVersionUID	= 1L;

		private final byte			state;

		public StateChange(byte state) {
			this.state = state;
		}

		@Override
		public void undo() {
			AjaxLazyLoadImage.this.state = state;
		}
	}
}
