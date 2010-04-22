/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.tool;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.profile2.tool.pages.MyProfile;

public class ProfileApplication extends WebApplication {    
    	
	protected void init(){
		super.init();
		
		// Configure general Wicket application settings
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);
		
		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(MyProfile.class);
		getApplicationSettings().setAccessDeniedPage(MyProfile.class);
		
	}
	
	// Throw RuntimeExceptions so they are caught by the Sakai ErrorReportHandler
	@Override
	public RequestCycle newRequestCycle(Request request, Response response) {
		return new WebRequestCycle(this, (WebRequest)request, (WebResponse)response) {
			@Override
			public Page onRuntimeException(Page page, RuntimeException e) {
				throw e;
			}
		};
	}
	
	
	public ProfileApplication() {
	}
	
	//setup homepage		
	public Class<Dispatcher> getHomePage() {
		return Dispatcher.class;
	}

}
