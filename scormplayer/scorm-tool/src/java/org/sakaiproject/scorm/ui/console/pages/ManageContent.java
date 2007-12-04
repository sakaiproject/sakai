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
package org.sakaiproject.scorm.ui.console.pages;

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
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.player.pages.FilePickerPage;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class ManageContent extends SakaiPortletWebPage {
	private static final long serialVersionUID = 1L;
	private static final String MODE_PARAM = "mode";
	private static final int LIST_VIEW = 0;
	private static final int UPLOAD_VIEW = 1;
	
	private String message;

	@SpringBean
	ScormClientFacade clientFacade;
	
	public ManageContent(final PageParameters pageParams) {
		int mode = pageParams.getInt(MODE_PARAM, LIST_VIEW);
		
		/*switch (mode) {
		case LIST_VIEW:
			PageParameters listPageParameters = new PageParameters();
			listPageParameters.put("mode", LIST_VIEW);
			
			add(new Label("listLink"), )
			add(new BookmarkablePageLink("uploadLink", ManageContent.class, uploadPageParameters));
			break;
		case UPLOAD_VIEW:
			PageParameters uploadPageParameters = new PageParameters();
			uploadPageParameters.put("mode", UPLOAD_VIEW);
			
			
			break;
		default:
			
		
		}*/
		
		
		PageParameters listPageParameters = new PageParameters();
		listPageParameters.put("mode", LIST_VIEW);
		
		PageParameters uploadPageParameters = new PageParameters();
		uploadPageParameters.put("mode", UPLOAD_VIEW);
		
		
		add(newResourceLabel("title", this));
		
		
		add(new BookmarkablePageLink("uploadLink", ManageContent.class, uploadPageParameters));
		
		
		
		
	}
	
	public String getMessage() {
		return message;
	}
	
	
	
}
