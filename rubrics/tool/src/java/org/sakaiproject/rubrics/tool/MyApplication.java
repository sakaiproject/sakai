/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.tool;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.Url;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import org.sakaiproject.rubrics.tool.pages.FirstPage;

/**
 * Main application class for our app
 * 
 */
@Slf4j
public class MyApplication extends WebApplication {
   
	/**
	 * Configure app here
	 */

	@Override
	protected void init() {
		
		//Configure for Spring injection
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		//Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		
		//Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);
		
		//Don't add any extra tags around a disabled link (default is <em></em>)
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);
				
		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(FirstPage.class);
		getApplicationSettings().setAccessDeniedPage(FirstPage.class);

        getRequestCycleListeners().add(new IRequestCycleListener() {

            public void onBeginRequest()
            {
            // optionally do something at the beginning of the request
            }

            public void onEndRequest()
            {
            // optionally do something at the end of the request
            }

            public IRequestHandler onException(RequestCycle cycle, Exception ex)
            {
            // optionally do something here when there's an exception

            // then, return the appropriate IRequestHandler, or "null"
            // to let another listener handle the exception
                log.info(ex.getMessage());
                return null;
            }

            @Override
            public void onBeginRequest(RequestCycle arg0) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onDetach(RequestCycle arg0) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onEndRequest(RequestCycle arg0) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onExceptionRequestHandlerResolved(
                    RequestCycle arg0, IRequestHandler arg1, Exception arg2) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onRequestHandlerExecuted(RequestCycle arg0,
                                                 IRequestHandler arg1) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onRequestHandlerResolved(RequestCycle arg0,
                                                 IRequestHandler arg1) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onRequestHandlerScheduled(RequestCycle arg0,
                                                  IRequestHandler arg1) {
            // TODO Auto-generated method stub

            }

            @Override
            public void onUrlMapped(RequestCycle arg0,
                                    IRequestHandler arg1, Url arg2) {
            // TODO Auto-generated method stub

            }
        });
	}
	

	
	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<FirstPage> getHomePage() {
		return FirstPage.class;
	}
	
	
	/**
     * Constructor
     */
	public MyApplication()
	{
	}
	
	

}
