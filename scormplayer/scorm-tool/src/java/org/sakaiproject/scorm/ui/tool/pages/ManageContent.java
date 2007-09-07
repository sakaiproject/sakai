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
package org.sakaiproject.scorm.ui.tool.pages;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class ManageContent extends SakaiPortletWebPage {
	private static final long serialVersionUID = 1L;

	private String message;

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
			private static final long serialVersionUID = 1L;
			
		 	public void populateItem(final ListItem item) {
		 		final ContentResourceWrapper resource = (ContentResourceWrapper)item.getModelObject();
		 		
		 		String id = resource.getId();
		 		String[] parts = id.split("/");
		 		
		 		final String fileName = parts[parts.length - 1];
		 		final PageParameters params = new PageParameters();
		 		params.add("contentPackage", resource.getId());
	 		
		 		if (null != parts && parts.length > 0) {
		 			item.add(new Label("packageName", fileName));
		 			
		 			item.add(new BookmarkablePageLink("launch", View.class, params)
		 				.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | 
		 						PopupSettings.SCROLLBARS).setWindowName("SCORMPlayer")));
		 		}
		 	}
		});
	}
	
	public String getMessage() {
		return message;
	}
	
	public class ContentResourceWrapper implements Serializable {
		private static final long serialVersionUID = 1L;
		
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
	
}
