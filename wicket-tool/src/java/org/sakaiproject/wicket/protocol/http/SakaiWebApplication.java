/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.protocol.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.wicket.markup.html.SakaiSessionExpiredPage;

public abstract class SakaiWebApplication extends WebApplication {
	private static Log log = LogFactory.getLog(SakaiWebApplication.class);
	
	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getDebugSettings().setAjaxDebugModeEnabled(log.isDebugEnabled());	
		getApplicationSettings().setPageExpiredErrorPage(SakaiSessionExpiredPage.class);
	}
	
	/**
	 * Overriding this method to obfuscate all urls
	 */
	/*protected IRequestCycleProcessor newRequestCycleProcessor()
	{
	    return new WebRequestCycleProcessor()
	    {
	        protected IRequestCodingStrategy newRequestCodingStrategy()
	        {
	            return new CryptedUrlWebRequestCodingStrategy(new WebRequestCodingStrategy());
	        }
	    };
	}*/

}
