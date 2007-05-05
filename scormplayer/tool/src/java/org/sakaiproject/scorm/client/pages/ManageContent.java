/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.client.pages;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.client.ScormTool;
import org.sakaiproject.scorm.client.api.ScormClientFacade;

import wicket.Response;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PopupSettings;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.util.string.JavascriptUtils;

public class ManageContent extends SakaiWicketPage {
	private static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name );";
	
	//public static final String API_JSCLASS_BEGIN = "if (typeof(window.API_1484_11) == 'undefined') \n" 
	//	+ " window.API_1484_11 = {  \n";
	//public static final String API_TESTMETHOD1_BEGIN = " window.API_1484_11.TestMethod1 { \n";
	//public static final String API_TESTMETHOD2_BEGIN = " window.API_1484_11.TestMethod2 { \n";
	
	public static final String API_METHOD_BEGIN = " { \n";
	public static final String API_METHOD_END = " }\n	}; \n";
	//public static final String API_JSCLASS_END = " }; \n";
	
	private String message;
	private Label contentLabel;
	
	private int numberOfClicks = 0;
	
	@SuppressWarnings("serial")
	public ManageContent() {		
		ScormClientFacade clientFacade = ((ScormTool)getApplication()).getClientFacade();

		final String contextId = clientFacade.getContext();
		
		List<ContentResource> contentPackages = clientFacade.getContentPackages();
		List<ContentResourceWrapper> contentPackageWrappers = new LinkedList<ContentResourceWrapper>();
		
		for (ContentResource resource : contentPackages) {
			contentPackageWrappers.add(new ContentResourceWrapper(resource));
		}
		
		add(new ListView("rows", contentPackageWrappers) {
		 	public void populateItem(final ListItem item) {
		 		ContentResourceWrapper resource = (ContentResourceWrapper)item.getModelObject();
		 		
		 		String id = resource.getId();
		 		String[] parts = id.split("/");
		 		
		 		final String fileName = parts[parts.length - 1];
		 		
		 		StringBuffer url = new StringBuffer();
		 		url.append("/portal/directtool/sakai.scorm.tool")
		 			.append("?package=").append(fileName)
		 			.append("&sakai.site=")
		 			.append(contextId);
		 		
		 		if (null != parts && parts.length > 0) {
		 			item.add(new ExternalLink("url", url.toString(), fileName));
		 			
		 			item.add(new Link("launch") {
		 				public void onClick() {
		 					//setResponsePage(new LaunchPackage(fileName));
		 					//setResponsePage(new SimpleTreePage());
		 					setResponsePage(new LaunchFrameset());
		 				}
		 			}.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS)));
		 		}
		 	}
		});
		
		/*this.add(new AbstractAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			protected String getImplementationId()
			{
				return "ParentAPI";
			}

			protected void onRenderHeadInitContribution(Response response)
			{
				super.onRenderHeadInitContribution(response);
				
			}

			public void onRequest()
			{
			}
		});*/
		
		//if (typeof(Wicket) == "undefined")
		//	Wicket = { };
		
		//addScormJavascriptAPI();
	}
	
	public void onAttach() {
		getBodyContainer().addOnLoadModifier(BODY_ONLOAD_ADDTL, null);
	}
		
	public void onTestMethod1(final AjaxRequestTarget target) {
		System.out.println("TEST 1 CLICKED!");
		numberOfClicks++;
		this.message = "hello new world!!! " + numberOfClicks + " times!";
		target.addComponent(contentLabel);
		//target.appendJavascript("return 'Hello new world';");
	}
	
	public void onTestMethod2(final AjaxRequestTarget target) {
		System.out.println("TEST 2 CLICKED!");
	}
	
	public String getMessage() {
		return message;
	}
	
	private void addScormJavascriptAPI() {
		contentLabel = new Label("SCORM_API", new PropertyModel(this, "message"));
		
		contentLabel.add(new ScormActionBehavior("SCORM_API","onTestMethod1", "API_1484_11 = { TestMethod1: function() ") {
			private static final long serialVersionUID = 1L;
			protected void onEvent(AjaxRequestTarget target)
			{
				onTestMethod1(target);
			}
		});
		
		/*contentLabel.add(new ScormActionBehavior("SCORM_API","onTestMethod2", "window.API_1484_11 = { TestMethod2: function() ") {
			private static final long serialVersionUID = 2L;
			protected void onEvent(AjaxRequestTarget target)
			{
				onTestMethod2(target);
			}
		});*/
				
		/*contentLabel.add(new AjaxEventBehavior("API_1484_11")
		{
			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				onTestApiRequest(target);
			}

			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator();
			}
			
			protected void onRenderHeadInitContribution(Response response)
			{
				super.onRenderHeadInitContribution(response);
				
				StringBuffer script = new StringBuffer().append(API_JSCLASS_BEGIN)
					.append(getCallbackScript()).append(API_JSCLASS_END);
				
				JavascriptUtils.writeJavascript(response, script.toString(), "API_1484_11_JAVASCRIPT");
			}

		});*/
		
		add(contentLabel);		
	}
	
	
	public final class ScormActionAjaxCallDecorator extends AjaxPostprocessingCallDecorator {
		private static final long serialVersionUID = 1L;
		private String componentId;
		
		public ScormActionAjaxCallDecorator(String componentId) {
			this((IAjaxCallDecorator)null);
			this.componentId = componentId;
		}
		
		public ScormActionAjaxCallDecorator(IAjaxCallDecorator delegate) {
			super(delegate);
		}

		public CharSequence postDecorateScript(CharSequence script)
		{
			StringBuffer buffer = new StringBuffer().append(script)
				.append(" return document.getElementById('").append(componentId).append("').value;");
			
			return buffer.toString();
		}
	}
	
	
	
	public abstract class ScormActionBehavior extends AjaxEventBehavior {
		private String componentId;
		private String methodName;
		
		public ScormActionBehavior(String componentId, final String event, String methodName) {
			super(event);
			this.methodName = methodName;
			this.componentId = componentId;
		}
		
		protected IAjaxCallDecorator getAjaxCallDecorator()
		{
			return new ScormActionAjaxCallDecorator(componentId);
		}
		
		protected void onRenderHeadInitContribution(Response response)
		{
			super.onRenderHeadInitContribution(response);
			
			//StringBuffer successScript = new StringBuffer()
			//	.append("\n return document.getElementById(").append(componentId).append(").outerHTML;");
			
			StringBuffer script = new StringBuffer().append(methodName).append(API_METHOD_BEGIN)
				.append(getCallbackScript())
				//.append(getCallbackScript("wicketAjaxGet('" + getCallbackUrl(true, false) + "'", successScript.toString(), null))
				//.append("\n return document.getElementById(").append(componentId).append(").")
				.append(API_METHOD_END);
			
			JavascriptUtils.writeJavascript(response, script.toString(), methodName);
		}
	}
	
	
	public class ContentResourceWrapper implements Serializable {
		private String id;
		private String url;
		
		public ContentResourceWrapper(ContentResource resource) {
			this.id = resource.getId();
			this.url = resource.getUrl();
		}
		
		public String getId() {
			return id;
		}
		
		public String getUrl() {
			return url;
		}
		
	}
	
	/*public class ContentProvider implements IDataProvider {
        
		public ContentProvider() { }
		
        public Iterator iterator(int first, int count) {
        	return getContentPackages().subList(first, first + count).iterator();
        }
        
        public int size() {
        	return getContentPackages().size();
        }
        
        public IModel model(Object object) {
        	return new Model(new ContentResourceWrapper((ContentResource)object));
        }
        
        private List getContentPackages() {
        	return ((ScormWicketApplication)getApplication()).getScormClientService().getContentPackages();
        }
        
	}*/
	
	
}
