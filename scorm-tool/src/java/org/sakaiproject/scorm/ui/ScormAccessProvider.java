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
package org.sakaiproject.scorm.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.AbortException;
import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.scorm.service.api.ScormEntityProvider;
import org.sakaiproject.util.ContextLoaderListener;

public class ScormAccessProvider implements HttpServletAccessProvider {

	protected ServletContext context = null;
	private boolean isInitialized = false;
	private ContextLoaderListener contextLoaderListener = null;
	private ServletContextEvent event = null;
	
	private ScormAccessProviderApplication webApplication;
	private HttpServletAccessProviderManager providerManager;
	
	public void init() {
		providerManager.registerProvider(ScormEntityProvider.ENTITY_PREFIX, this);
		/*try {
			init(null);
		} catch (ServletException se) {
			se.printStackTrace();
		}*/
	}
	
	public void init(FilterConfig filterConfig) throws ServletException
	{
		
		if (filterConfig == null) {
			
			if (isInitialized)
				return;
			isInitialized = true;
			
			final Map<String, String> initParamMap = new HashMap<String, String>();
			
			initParamMap.put("applicationFactoryClassName", "org.apache.wicket.spring.SpringWebApplicationFactory");
			initParamMap.put("applicationBean", "scormAccessProviderApplication");
			
			filterConfig = new FilterConfig() {
	
				public ServletContext getServletContext() {
					return context;
				}
	
				public Enumeration getInitParameterNames() {
					return new Vector(initParamMap.keySet()).elements();
				}
	
				public String getInitParameter(String name) {
					return initParamMap.get(name);
				}
	
				public String getFilterName() {
					return "sakai.scorm.access.filter";
				}
			}; 
		}
		
		//super.init(filterConfig);
	}
	
	/*public void destroy()
	{
		super.destroy();
		
		if (contextLoaderListener != null && event != null)
			contextLoaderListener.contextDestroyed(event);
	}*/
	
	public void handleAccess(HttpServletRequest servletRequest, HttpServletResponse servletResponse, EntityReference ref) {
		
		System.out.println("Entering handleAccess");
		
		System.out.println("Request URI: " + servletRequest.getRequestURI());
		
		String requestUri = servletRequest.getRequestURI();
		String prefix = "/direct/scorm";
		
		String relativePath = "/";
		
		if (requestUri.startsWith(prefix))
			relativePath = requestUri.substring(prefix.length());
			//getRelativePath(servletRequest);	

		System.out.println("Relative path: " + relativePath);
		
		// Special-case for home page - we redirect to add a trailing slash.
		if (relativePath.length() == 0
				&& !Strings.stripJSessionId(servletRequest.getRequestURI()).endsWith("/"))
		{
			final String redirectUrl = servletRequest.getRequestURI() + "/";
			try {
				servletResponse.sendRedirect(redirectUrl);
				return;
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		System.out.println("A");

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = Thread.currentThread().getContextClassLoader();
			//getClassLoader();
		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			// If the request does not provide information about the encoding of
			// its body (which includes POST parameters), than assume the
			// default encoding as defined by the wicket application. Bear in
			// mind that the encoding of the request usually is equal to the
			// previous response.
			// However it is a known bug of IE that it does not provide this
			// information. Please see the wiki for more details and why all
			// other browser deliberately copied that bug.
			if (servletRequest.getCharacterEncoding() == null)
			{
				try
				{
					// The encoding defined by the wicket settings is used to
					// encode the responses. Thus, it is reasonable to assume
					// the request has the same encoding. This is especially
					// important for forms and form parameters.
					servletRequest.setCharacterEncoding(webApplication.getRequestCycleSettings()
							.getResponseRequestEncoding());
				}
				catch (UnsupportedEncodingException ex)
				{
					throw new WicketRuntimeException(ex.getMessage());
				}
			}

			System.out.println("B");
			
			// Create a new webrequest
			final WebRequest request = webApplication.newWebRequest(servletRequest);
				//new ServletWebRequest(servletRequest);

			System.out.println("C");
			
			// Are we using REDIRECT_TO_BUFFER?
			/*if (webApplication.getRequestCycleSettings().getRenderStrategy() == IRequestCycleSettings.REDIRECT_TO_BUFFER)
			{
				String queryString = servletRequest.getQueryString();
				if (!Strings.isEmpty(queryString))
				{
					// Try to see if there is a redirect stored
					ISessionStore sessionStore = webApplication.getSessionStore();
					String sessionId = sessionStore.getSessionId(request, false);
					if (sessionId != null)
					{
						BufferedHttpServletResponse bufferedResponse = webApplication
								.popBufferedResponse(sessionId, queryString);

						if (bufferedResponse != null)
						{
							bufferedResponse.writeTo(servletResponse);
							// redirect responses are ignored for the request
							// logger...
							return;
						}
					}
				}
			}*/

			// First, set the web application for this thread
			Application.set(webApplication);

System.out.println("D");
			
			// Create a response object and set the output encoding according to
			// wicket's application settings.
			final WebResponse response = webApplication.newWebResponse(servletResponse);
			response.setAjax(request.isAjax());
			response.setCharacterEncoding(webApplication.getRequestCycleSettings()
					.getResponseRequestEncoding());

System.out.println("E");			
			try
			{
				// Create request cycle
				RequestCycle cycle = webApplication.newRequestCycle(request, response);

				try
				{
					// Process request
					cycle.request();
					System.out.println("After request");
				}
				catch (AbortException e)
				{
					// noop
				}
System.out.println("F");
			}
			finally
			{
				// Close response
				response.close();

				// Clean up thread local session
				Session.unset();

				// Clean up thread local application
				Application.unset();
			}
		}
		finally
		{
			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
		
	}
	
	protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
	{
		return (webApplication.getRequestCycleSettings().getBufferResponse() ? new BufferedWebResponse(
				servletResponse) {
			public CharSequence encodeURL(final CharSequence url)
			{
				System.out.println("Initial url: " + url);
				
				String stringUrl = (String)url;
				StringBuffer buffer = new StringBuffer();
				
				int indexOf = stringUrl.indexOf('/');
				int lastIndexOf = 0;
				while (indexOf != -1) {
					String chunk = stringUrl.substring(lastIndexOf, indexOf);
					
					System.out.println("CHUNK: " + chunk);
					if (chunk.equals(".."))
						buffer.append(chunk).append("/");
					else
						break;
					
					lastIndexOf = indexOf + 1;
					indexOf = stringUrl.indexOf('/', lastIndexOf);
				}
				
				buffer.append("scorm/");
				buffer.append(stringUrl.substring(lastIndexOf));
				
				System.out.println("Last index: " + lastIndexOf);
				
				System.out.println("Final url: " + buffer.toString());
				
				return buffer.toString();
			}		
		} : new WebResponse(servletResponse) {
					public CharSequence encodeURL(final CharSequence url)
					{
						System.out.println("Initial url: " + url);
						
						String stringUrl = (String)url;
						StringBuffer buffer = new StringBuffer();
						
						int indexOf = stringUrl.indexOf('/');
						int lastIndexOf = 0;
						while (indexOf != -1) {
							String chunk = stringUrl.substring(lastIndexOf, indexOf);
							
							System.out.println("CHUNK: " + chunk);
							if (chunk.equals(".."))
								buffer.append(chunk).append("/");
							else
								break;
							
							lastIndexOf = indexOf + 1;
							indexOf = stringUrl.indexOf('/', lastIndexOf);
						}
						
						buffer.append("scorm/");
						buffer.append(stringUrl.substring(lastIndexOf));
						
						System.out.println("Last index: " + lastIndexOf);
						
						System.out.println("Final url: " + buffer.toString());
						
						return buffer.toString();
					}
		});
	}

	public ScormAccessProviderApplication getWebApplication() {
		return webApplication;
	}

	public void setWebApplication(ScormAccessProviderApplication webApplication) {
		this.webApplication = webApplication;
	}


	public HttpServletAccessProviderManager getProviderManager() {
		return providerManager;
	}


	public void setProviderManager(HttpServletAccessProviderManager providerManager) {
		this.providerManager = providerManager;
	}


	
}
