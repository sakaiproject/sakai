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
package org.sakaiproject.scorm.tool.pages;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.utils.ApiAjaxBean;

public class ManageContent extends ClientPage {
	private static final long serialVersionUID = 1L;
	private static final ResourceReference API = new CompressedResourceReference(ManageContent.class, "API.js");
	
	private ApiAjaxBean bean = new ApiAjaxBean();
	private String message;
	private Label contentLabel;
	
	private int numberOfClicks = 0;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	public ManageContent(final PageParameters pageParams) {
		add(newResourceLabel("title", this));
		
		List<ContentResource> contentPackages = clientFacade.getContentPackages();
		List<ContentResourceWrapper> contentPackageWrappers = new LinkedList<ContentResourceWrapper>();
		
		for (ContentResource resource : contentPackages) {
			contentPackageWrappers.add(new ContentResourceWrapper(resource));
		}
		
		add(new ListView("rows", contentPackageWrappers) {
		 	public void populateItem(final ListItem item) {
		 		final ContentResourceWrapper resource = (ContentResourceWrapper)item.getModelObject();
		 		
		 		String id = resource.getId();
		 		String[] parts = id.split("/");
		 		
		 		final String fileName = parts[parts.length - 1];
		 		final PageParameters pageParams = new PageParameters();
		 		pageParams.add("contentPackage", resource.getId());
	 		
		 		if (null != parts && parts.length > 0) {
		 			item.add(new Label("packageName", fileName));
		 			
		 			item.add(new BookmarkablePageLink("launch", View.class, pageParams)
		 				.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | 
		 						PopupSettings.SCROLLBARS).setWindowName("SCORMPlayer")));
		 		}
		 	}
		});
		
		/*final ResourceReference[] references = new ResourceReference[] { API };
		final Form form = new Form("form", new CompoundPropertyModel(bean));
		add(form);
		form.setOutputMarkupId(true);
		
		FormComponent arg1 = new HiddenField("arg1");		
		arg1.setOutputMarkupId(true);
		form.add(arg1);
		
		FormComponent arg2 = new HiddenField("arg2");		
		arg2.setOutputMarkupId(true);
		form.add(arg2);
		
		FormComponent resultComponent = new HiddenField("result");
		resultComponent.setOutputMarkupId(true);
		form.add(resultComponent);
		
		form.add(new ApiAjaxMethod(form, "GetDiagnostic", references, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);
				return errorManager.getErrorDiagnostic(arg);
			}
		});*/
		
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
	
	public String getMessage() {
		return message;
	}
	
	/*private void addScormJavascriptAPI() {
		contentLabel = new Label("SCORM_API", new PropertyModel(this, "message"));
		
		contentLabel.add(new ScormActionBehavior("SCORM_API","onTestMethod1", "API_1484_11 = { TestMethod1: function() ") {
			private static final long serialVersionUID = 1L;
			protected void onEvent(AjaxRequestTarget target)
			{
				onTestMethod1(target);
			}
		});
		
		
		add(contentLabel);		
	}*/
	
	private String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";
		
		return argumentValues.get(0);
	}
	
	
	/*public final class ScormActionAjaxCallDecorator extends AjaxPostprocessingCallDecorator {
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
		
		public void renderHead(IHeaderResponse response) {
		    super.renderHead(response);
		    
		    StringBuffer script = new StringBuffer().append(methodName).append(API_METHOD_BEGIN)
			.append(getCallbackScript())
			.append(API_METHOD_END);
		
		    response.renderJavascript(script.toString(), methodName);
		}
	}*/
	
	
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
