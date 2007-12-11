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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.AbortException;
import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WicketServlet;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.scorm.entity.api.ScormEntityReference;
import org.sakaiproject.scorm.service.api.ScormEntityProvider;

public class ScormAccessProviderApplication extends WebApplication implements HttpServletAccessProvider {

	HttpServletAccessProviderManager providerManager;
	
	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));

		providerManager.registerProvider(ScormEntityProvider.ENTITY_PREFIX, this);
		
		getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.ONE_PASS_RENDER);
		
		//this.mountBookmarkablePage("navigate", "navFrame", NavigationFrame.class);
		//this.mountBookmarkablePage("content", "contentFrame", ContentFrame.class);
		//this.mountBookmarkablePage("manage", "contentFrame", ManageContent.class);
		//this.mountBookmarkablePage("launch", "launch", LaunchFrameset.class);
		
		//this.mount("launch", PackageName.forClass(LaunchFrameset.class));
		
	
		WicketServlet servlet;
		
		//WicketFilter filter = getWicketFilter();
		
		/*if (filter instanceof ScormAccessProvider) {
			System.out.println("This filter is a ScormAccessProvider");
			ScormAccessProvider scormAccessProvider = (ScormAccessProvider)filter;
			providerManager.registerProvider(ScormEntityProvider.ENTITY_PREFIX, scormAccessProvider);	
		}*/
		
		/*final ServletContext context = this.getServletContext();

		final Map<String, String> initParamMap = new HashMap<String, String>();
		
		initParamMap.put("applicationFactoryClassName", "org.apache.wicket.spring.SpringWebApplicationFactory");
		initParamMap.put("applicationBean", "scormAccessProviderApplication");
		
		try {
			scormAccessProvider.init(new FilterConfig() {

				public ServletContext getServletContext()
				{
					return context;
				}

				public Enumeration getInitParameterNames()
				{
					return new Vector(initParamMap.keySet()).elements();
				}

				public String getInitParameter(String name)
				{
					return initParamMap.get(name);
				}

				public String getFilterName()
				{
					return "sakai.scorm.access.filter";
				}
			}); 
		} catch (ServletException e) {
				throw new RuntimeException(e);
		} 
		
		providerManager.registerProvider(ScormEntityProvider.ENTITY_PREFIX, scormAccessProvider);
		*/
		/*this.mount(new URIRequestTargetUrlCodingStrategy("/scorm") {
			
			public CharSequence encode(IRequestTarget requestTarget) {
				final AppendingStringBuffer url = new AppendingStringBuffer(40);
				url.append(getMountPath());
			
				if ((requestTarget instanceof IBookmarkablePageRequestTarget))
				{
					final IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
	
					PageParameters pageParameters = target.getPageParameters();
					String pagemap = target.getPageMapName();
					if (pagemap != null) {
						if (pageParameters == null) {
							pageParameters = new PageParameters();
						}
						pageParameters.put(WebRequestCodingStrategy.PAGEMAP, pagemap);
					}
					if (pageParameters != null)
						appendParameters(url, pageParameters);
				} else if (requestTarget instanceof ISharedResourceRequestTarget) {
					url.append(getMountPath());
					final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;
	
					RequestParameters requestParameters = target.getRequestParameters();
					
					if (requestParameters != null) 
						appendParameters(url, requestParameters.getParameters());
				}
				return url;
			}
			
			
			public IRequestTarget decode(RequestParameters requestParameters)
			{
				return null;
			}
			
			public boolean matches(IRequestTarget requestTarget)
			{
				return true;
			}
			
		});*/
		
		
		
		/*this.mount(new SharedResourceRequestTargetUrlCodingStrategy("/scorm/resources", "") {
			
			public IRequestTarget decode(RequestParameters requestParameters)
			{
				final String parametersFragment = requestParameters.getPath().substring(
						getMountPath().length());
				final ValueMap parameters = decodeParameters(parametersFragment, requestParameters
						.getParameters());

				for (Object paramKey : parameters.keySet()) {
					String key = (String)paramKey;
					String value = parameters.getString(key);
					
					System.out.println("KEY: " + key);
					System.out.println("VALUE: " + value);
					System.out.println();
				}
				
				requestParameters.setParameters(parameters);
				requestParameters.setResourceKey("blah");
				return new SharedResourceRequestTarget(requestParameters);
			}

			public boolean matches(IRequestTarget requestTarget)
			{
				if (requestTarget instanceof ISharedResourceRequestTarget)
				{
					//ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;
					return true; //target.getRequestParameters().getResourceKey().equals(resourceKey);
				}
				else
				{
					return false;
				}
			}
			
		});*/
		
		/*this.mountSharedResource("/scorm/resource/org.sakaiproject.scorm.tool.pages.ContentFrame/API.js", "org.sakaiproject.scorm.tool.pages.ContentFrame/API.js"); //"org.sakaiproject.scorm.tool.pages.ContentFrame/API.js");
		this.mountSharedResource("/scorm/resource/org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/wicket-ajax.js", "org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/wicket-ajax.js");
		this.mountSharedResource("/scorm/resource/org.apache.wicket.markup.html.WicketEventReference/wicket-event.js", "org.apache.wicket.markup.html.WicketEventReference/wicket-event.js");
		this.mountSharedResource("/scorm/resource/org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/wicket-ajax-debug.js", "org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/wicket-ajax-debug.js");
		*/
		
		
		//System.out.println("Mounted shared resource " + ApiPanel.API.getSharedResourceKey());	
		
		//this.mount("/scorm", PackageName.forClass(LaunchFrameset.class));
		//this.mount("/scorm/resources", PackageName.forClass(ApiPanel.class));
		//this.mount("/scorm/sysresources", PackageName.forClass(AbstractDefaultAjaxBehavior.class));
	}
	
	@Override
	public Class getHomePage() {
		return null;
	}

	/*protected IRequestCycleFactory getRequestCycleFactory()
	{
		return new IRequestCycleFactory()
		{
			private static final long serialVersionUID = 1L;

			public RequestCycle newRequestCycle(final Application application,
					final Request request, final Response response)
			{
				// Respond to request
				return new WebRequestCycle((WebApplication)application, (WebRequest)request,
						(WebResponse)response);
			}
		};
	}*/
	
	/*protected IRequestCycleProcessor newRequestCycleProcessor()
	{
		return new WebRequestCycleProcessor() {
			protected IRequestCodingStrategy newRequestCodingStrategy()
			{
				return new WebRequestCodingStrategy() {
					protected String doEncode(RequestCycle requestCycle, IRequestTarget requestTarget) {
						return ;
					}
				};
	
				
				return new IRequestCodingStrategy() {

					public RequestParameters decode(Request arg0) {
						// TODO Auto-generated method stub
						return null;
					}

					public CharSequence encode(RequestCycle arg0, IRequestTarget arg1) {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}
		};
	}*/
	
	protected WebRequest newWebRequest(final HttpServletRequest servletRequest) {
		return new ServletWebRequest(servletRequest) {
			public String getURL() {
				String url = super.getURL();
				
				return decodeScormURL(url);
			}
			
			public String getPath() {
				String path = super.getPath();
				
				return decodeScormURL(path);
			}
			
			public String getRelativePathPrefixToContextRoot() {
				String path = super.getRelativePathPrefixToContextRoot();
				
				System.out.println("Relative path prefix to context root: " + path);
				
				return path;
			}
			
			public String getRelativePathPrefixToWicketHandler() {
				String path = super.getRelativePathPrefixToWicketHandler();
				
				path += "scorm/";
				System.out.println("Relative path prefix to wicket: " + path);
				
				return path;
			}
			
			public String getServletPath() {
				String path = super.getServletPath();
				
				System.out.println("Servlet path: " + path);
				
				return path;
			}
		};	
	}
	
	/*protected WebResponse newWebResponse(final HttpServletResponse servletResponse) {
		return super.newWebResponse(servletResponse);	
	}*/
	
	final String decodeScormURL(final String unparsed) {
		String parsed = new String(unparsed);
		
		System.out.println("Unparsed: " + unparsed);
		
		while (parsed.startsWith("scorm/"))
			parsed = parsed.substring(6);
		
		System.out.println("Parsed: " + parsed);

		return parsed;
	}
	
	final CharSequence encodeScormURL(final CharSequence url) {
		System.out.println("Initial url: " + url);
		
		String stringUrl = String.valueOf(url);
		StringBuffer buffer = new StringBuffer();
		
		int indexOf = stringUrl.indexOf('/');
		int lastIndexOf = 0;
		while (indexOf != -1) {
			String chunk = stringUrl.substring(lastIndexOf, indexOf);
			
			//System.out.println("CHUNK: " + chunk);
			if (chunk.equals(".."))
				buffer.append(chunk).append("/");
			else if (chunk.equals("scorm"))
				break;
			else {
				buffer.append("scorm/");
				break;
			}
			
			lastIndexOf = indexOf + 1;
			indexOf = stringUrl.indexOf('/', lastIndexOf);
		}
		

		buffer.append(stringUrl.substring(lastIndexOf));
		
		System.out.println("Last index: " + lastIndexOf);
		
		System.out.println("Final url: " + buffer.toString());
		
		return buffer.toString();
	}
	
	
	protected WebResponse newWebResponse(final HttpServletResponse servletResponse) {
		return (getRequestCycleSettings().getBufferResponse() ? new BufferedWebResponse(
				servletResponse) {
			public CharSequence encodeURL(final CharSequence url) {
				return super.encodeURL(url);
			}		
		} : new WebResponse(servletResponse) {
			public CharSequence encodeURL(final CharSequence url) {
				return super.encodeURL(url);
			}
		});
	}
	
	public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference ref) {
	
		Application.set(this);


		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = this.getClass().getClassLoader();
	
		System.out.println("Class loader (prev): " + previousClassLoader.toString());
		System.out.println("Class loader (new): " + newClassLoader.toString());
		try {
			if (previousClassLoader != newClassLoader) {
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
			if (req.getCharacterEncoding() == null)
			{
				try
				{
					// The encoding defined by the wicket settings is used to
					// encode the responses. Thus, it is reasonable to assume
					// the request has the same encoding. This is especially
					// important for forms and form parameters.
					req.setCharacterEncoding(this.getRequestCycleSettings()
							.getResponseRequestEncoding());
				}
				catch (UnsupportedEncodingException ex)
				{
					throw new WicketRuntimeException(ex.getMessage());
				}
			}
			
			WebRequest request = newWebRequest(req);
			
			WebResponse response = newWebResponse(res);
				
			response.setAjax(request.isAjax());
			response.setCharacterEncoding(this.getRequestCycleSettings().getResponseRequestEncoding());
			
			/*	new WebResponse(res) {
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
			};*/
			
				
			/*String mountPath = "/scorm/launch/";
			
			BookmarkablePageRequestTargetUrlCodingStrategy strategy = 
				new BookmarkablePageRequestTargetUrlCodingStrategy(mountPath,
						LaunchFrameset.class, "launch");
			
			cycle.getProcessor().getRequestCodingStrategy().mount(strategy);*/
				
			//IRequestCodingStrategy encoder
			 
			
			//cycle.setResponsePage(LaunchFrameset.class, pageParams);
			
			
			//IdEntityReference scormRef = (IdEntityReference)ref;
			
			
			RequestCycle cycle = newRequestCycle(request, response);
	
			
			ScormEntityReference scormRef = (ScormEntityReference)ref;
			
			//System.out.println("idRef id: " + scormRef.id);
			//System.out.println("idRef: " + scormRef.toString());
			//System.out.println("idRef: " + scormRef.key);
			//String resourceKey = ScormEntityReference.getKey(scormRef.toString());
			
			/*boolean foundPage = true;
			
			String translatedKey = //scormRef.key.replace('/', ':');
				":group:simple-test-site:SCORM2004.3.BKME.1.0.zip";
				//"/group/simple-test-site/SCORM2004.3.BKME.1.0.zip";
				
			PageParameters pageParams = new PageParameters();
			pageParams.add("contentPackage", translatedKey); //"/group/simple-test-site/SCORM2004.3.BKME.1.0.zip");
			
			if (scormRef.id.equals("launch")) 
				cycle.setRequestTarget(new BookmarkablePageRequestTarget("launch", LaunchFrameset.class, pageParams));
			else if (scormRef.id.equals("navigate"))
				cycle.setRequestTarget(new BookmarkablePageRequestTarget("navFrame", NavigationFrame.class, pageParams));
			else if (scormRef.id.equals("content"))
				cycle.setRequestTarget(new BookmarkablePageRequestTarget("contentFrame", ContentFrame.class, pageParams));
			else if (scormRef.id.equals("resource")) {
				//System.out.println("Grabbing resourceKey: " + scormRef.key);
				RequestParameters requestParameters = new RequestParameters();
				requestParameters.setResourceKey(scormRef.key);
				cycle.setRequestTarget(new SharedResourceRequestTarget(requestParameters));
			}*/
	
	
			System.out.println("Render strategy: " + getRequestCycleSettings().getRenderStrategy());
			
			
			/*IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			CharSequence url = 
				encoder.encode(cycle, new BookmarkablePageRequestTarget("launch", LaunchFrameset.class, pageParams));
			
			RequestDispatcher dispatcher = req.getRequestDispatcher((String)url);
			
			try {
				dispatcher.forward(req, res);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
				
			/*try {
				getWicketFilter().doGet(req, res);
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}*/
				
			/*Request request = new ServletWebRequest(req);
			Response response = new WebResponse(res);
			RequestCycle cycle = getRequestCycleFactory().newRequestCycle((Application)this, request, response);
			
			PageParameters pageParams = new PageParameters();
			
			//if (ref instanceof IdEntityReference)
			//	pageParams.add("contentPackage", ((IdEntityReference)ref).id);
			
			pageParams.add("contentPackage", "SCORM2004.3.BKME.1.0.zip"); 
			
			cycle.setResponsePage(LaunchFrameset.class, pageParams);*/
			
			//if (foundPage) {
			try
			{
				try
				{
					System.out.println("Issuing cycle request");
					
					System.out.println("Request URL: " + request.getURL());
					
					
					RequestParameters params = request.getRequestParameters();
					
					System.out.println("Path: " + params.getPath());
					System.out.println("Component path: " + params.getComponentPath());
					
					/*Iterator it = params.getParameters().keySet().iterator();
					
					System.out.println("Number of request parameters = " + params.getParameters().size());
					
					while (it.hasNext()) {
						String key = (String)it.next();
						String value = (String)params.getParameters().get(key);
						
						System.out.println("KEY: " + key + " VALUE: " + value);
						
					}*/
					
					// Process request
					cycle.request();
				}
				catch (AbortException e)
				{
					// noop
				}
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
		} finally {
			if (newClassLoader != previousClassLoader) {
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
		
		
		//}
		
		/*PrintWriter writer = null;
		
		try {
			writer = res.getWriter();
			writer.println("Hello new world!");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Here I am!");*/
	}

	
	public HttpServletAccessProviderManager getProviderManager() {
		return providerManager;
	}

	public void setProviderManager(HttpServletAccessProviderManager providerManager) {
		this.providerManager = providerManager;
	}


}
