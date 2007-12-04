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
package org.sakaiproject.scorm.ui.player.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;

public class CompletionPage extends NotificationPage {
	//private static final ResourceReference JQUERY_JS = new CompressedResourceReference(CompletionPage.class, "res/jquery-latest.pack.js");
	//private static final ResourceReference THICKBOX_JS = new CompressedResourceReference(CompletionPage.class, "res/thickbox-compressed.js");
	//private static final ResourceReference THICKBOX_CSS = new CompressedResourceReference(CompletionPage.class, "res/thickbox.css");

	//private static final String ONLOAD_JS = "tb_show('Google', 'http://www.google.com', null);";
	//private static final String UNLOAD_JS = "tb_remove();";
	
	private static final String CLOSE_ON_LOAD = "setTimeout('window.close()', 5000);";
	
	private static final long serialVersionUID = 1L;

	public CompletionPage() {
		this(new PageParameters());
	}
	
	public CompletionPage(PageParameters pageParams) {
		super();
	}
	
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.renderOnDomReadyJavascript(CLOSE_ON_LOAD);
		
		//response.renderCSSReference(THICKBOX_CSS);
		//response.renderJavascriptReference(JQUERY_JS);
		//response.renderJavascriptReference(THICKBOX_JS);
		//response.renderOnLoadJavascript(ONLOAD_JS);
		//response.renderOnBeforeUnloadJavascript(UNLOAD_JS);
	}
	
	
}
