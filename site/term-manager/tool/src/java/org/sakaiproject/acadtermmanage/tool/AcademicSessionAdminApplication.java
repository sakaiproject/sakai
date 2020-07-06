/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.acadtermmanage.tool;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.acadtermmanage.tool.pages.SemesterPage;

/**
 * 
 * 
 * Based on the Wicket 1-4 Archetype by Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AcademicSessionAdminApplication extends WebApplication {    
   
	@Override
	protected void init() {
		
		//Configure for Spring injection
	    getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		
		
		//Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		
		//Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);
				
		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(getHomePage());
		getApplicationSettings().setAccessDeniedPage(getHomePage());
		
		// Disable Wicket's loading of jQuery - we load Sakai's preferred version in BasePage.java
		getJavaScriptLibrarySettings().setJQueryReference(new PackageResourceReference(AcademicSessionAdminApplication.class, "empty.js"));
		
		getRequestCycleListeners().add(new AbstractRequestCycleListener() {
			@Override
			 public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				 if (ex instanceof RuntimeException) {
					 RuntimeException re = (RuntimeException)ex;
					 throw re;
				 }
				 return super.onException(cycle, ex);
			}
		});
		
	}
	
	
	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<? extends Page> getHomePage() {
		return SemesterPage.class;
	}

}
