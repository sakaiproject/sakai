/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.webapp.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResource2;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

/**
 * This filter provides a number of functions that many tomahawk components require.
 * <p>
 * In tomahawk versions up to and including 1.1.6, it is mandatory to define this filter in the application's
 * web.xml in order to use some tomahawk components. In Tomahawk version 1.1.7, this filter is now optional;
 * when defined it will be used as for earlier versions. When omitted, the same functionality is now
 * automatically provided via classes TomahawkFacesContextFactory and ServeResourcePhaseListener. 
 *
 * <h2>Response Buffering</h2>
 * 
 * When the request is for a JSF page, and the configured "resource manager"
 * requires response buffering then a response wrapper is created which
 * buffers the entire output from the current request in memory.
 * <p>
 * Tomahawk provides at least three "resource managers":
 * <ul>
 * <li> DefaultAddResources (the default) does require response buffering.
 * <li> NonBufferingAddResource does not require response buffering, but it
 * renders javascript on body and offer support in portlets enviroments. 
 * <li> StreamingAddResources does not, but only provides a subset of the
 * features of DefaultAddResources and therefore does not work with all
 * Tomahawk components.
 * </ul>
 * <p>
 * Only one "resource manager" may be configured for a webapp. See class
 * AddResourceFactory for further details on configuring this.
 * <p>
 * When DefaultAddResources is enabled (default behaviour), the resulting
 * response buffering does cause some unnecessary memory and performance
 * impact for pages where no component in the page actually registers a
 * resource that needs to be inserted into the page - but whether a page
 * does that or not cannot be known until after the page has been rendered.
 * In the rare case where a request to a JSF page generates non-html
 * output (eg a PDF is generated as a response to a submit of a jsf page),
 * the data is unfortunately buffered. However it is not post-processed,
 * because its http content-type header will not be "text/html" (see other
 * documentation for this class).
 *
 * <h2>Inserting Resources into HTML responses (DefaultAddResources)</h2>
 * 
 * After the response has been completely generated (and cached in memory),
 * this filter checks the http content-type header. If it is not html or xhtml,
 * then the data is simply send to the response stream without further processing.
 * 
 * For html or xhtml responses, this filter causes the data to be post-processed
 * to insert any "resources" registered via the AddResources framework. This
 * allows jsf components (and other code if it wants) to register data that
 * should be output into an HTML page, particularly into places like an html
 * "head" section, even when the component occurs after that part of the
 * response has been generated.
 * <p>
 * The default "resource manager" (DefaultAddResources) supports inserting
 * resources into any part of the generated page. The alternate class
 * StreamingAddResources does not; it does not buffer output and therefore
 * can only insert resources for a jsf component  into the page after the
 * point at which that component is rendered. In particular, this means that
 * components that use external javascript files will not work with that
 * "resource manager" as [script href=...] only works in the head section
 * of an html page.
 *
 * <h2>Serving Resources from the Classpath</h2>
 * 
 * Exactly what text gets inserted into an HTML page via a "resource"
 * (see above) is managed by the AddResources class, not this one. However
 * often it is useful for jsf components to be able to refer to resources
 * that are on the classpath, and in particular when the resource is in the
 * same jar as the component code. Servlet engines do not support serving
 * resources from the classpath. However the AddResources framework can be
 * used to generate a special url prefix that this filter can be mapped to,
 * allowing this to be done. In particular, many Tomahawk components use
 * this to bundle their necessary resources within the tomahawk jarfile.
 * <p>
 * When a request to such a url is found by this filter, the actual resource
 * is located and streamed back to the user (no buffering required). See the
 * AddResource class documentation for further details.
 *
 * <h2>Handling File Uploads</h2>
 * 
 * Sometimes an application needs to allow a user to send a file of data
 * to the web application. The html page needs only to include an input
 * tag with type=file; clicking on this will prompt the user for a file
 * to send. The browser will then send an http request to the server
 * which is marked as a "mime multipart request" with normal http post
 * parameters in one mime part, and the file contents in another mime part.
 * <p>
 * This filter also handles such requests, using the Apache HttpClient
 * library to save the file into a configurable local directory before
 * allowing the normal processing for the url that the post request
 * refers to. A number of configuration properties on this filter control
 * maximum file upload sizes and various other useful settings. See the
 * documentation for the init method for more details.
 * 
 * <h2>Avoiding Processing</h2>
 * 
 * When the ExtensionsFilter is enabled, and the DefaultAddResources
 * implementation is used then there is no way to avoid having the
 * response buffered in memory. However as long as the mime-type set
 * for the response data is <i>not</i> text/html then the data will
 * be written out without any modifications.
 * 
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 954965 $ $Date: 2010-06-15 11:58:31 -0500 (Mar, 15 Jun 2010) $
 */
public class ExtensionsFilter implements Filter {

    private Log log = LogFactory.getLog(ExtensionsFilter.class);
    
    private int _uploadMaxSize = 100 * 1024 * 1024; // 100 MB

    private int _uploadMaxFileSize = 100 * 1024 * 1024; // 100 MB

    private int _uploadThresholdSize = 1 * 1024 * 1024; // 1 MB

    private String _uploadRepositoryPath = null; //standard temp directory
    
    private boolean _cacheFileSizeErrors = false; 

    private ServletContext _servletContext;

    public static final String DOFILTER_CALLED = "org.apache.myfaces.component.html.util.ExtensionFilter.doFilterCalled";

    /**
     * Flag that indicates extensions filter initialization code has been done.
     * This flag is added on application map.
     */
    public static final String EXTENSIONS_FILTER_INITIALIZED = "org.apache.myfaces.tomahawk.EXTENSIONS_FILTER_INITIALIZED";

    /**
     * Init method for this filter.
     * <p>
     * The following filter init parameters can be configured in the web.xml file
     * for this filter:
     * <ul>
     * <li>uploadMaxFileSize</li>
     * <li>uploadThresholdSize</li>
     * <li>uploadRepositoryPath</li>
     * <li>uploadMaxSize</li>
     * <li>cacheFileSizeErrors</li>
     * </ul>
     * </p>
     * <p>
     * All size parameters may have the suffix "g" (gigabytes), "m" (megabytes) or "k" (kilobytes).
     * </p>
     * 
     * <h2>uploadMaxFileSize</h2>
     * 
     * Sets the maximum allowable size for uploaded files.
     * <p>
     * If the user attempts to upload a file which is larger than this, then the data <i>is</i>
     * transmitted from the browser to the server (this cannot be prevented with standard HTML
     * functionality). However the file will not be saved in memory or on disk. An error message
     * will be added to the standard JSF error messages, and the page re-rendered (as for a
     * validation failure).
     * </p>
     * <p>
     * The default value is 100 Megabytes.
     * </p>
     * 
     * <h2>uploadThresholdSize</h2>
     * 
     * Sets the size threshold beyond which files are written directly to disk. Files which are
     * smaller than this are simply held in memory. The default is 1 Megabyte.
     * 
     * <h2>uploadRepositoryPath</h2>
     * 
     * Sets the directory in which temporary files (ie caches for those uploaded files that
     * are larger than uploadThresholdSize) are to be stored.
     * 
     * <h2>uploadMaxSize</h2>
     * 
     * Sets the maximum allowable size for the current request. If not set, its value is the 
     * value set on uploadMaxFileSize param. 
     * 
     * <h2>cacheFileSizeErrors</h2>
     * 
     * Catch and swallow FileSizeLimitExceededExceptions in order to return as
     * many usable items as possible.
     * 
     */
    public void init(FilterConfig filterConfig) {
        // Note that the code here to extract FileUpload configuration params is not actually used.
        // The handling of multipart requests was moved from this Filter into a custom FacesContext
        // (TomahawkFacesContextWrapper) so that Portlets could be supported (Portlets cannot use
        // servlet filters).
        //
        // For backwards compatibility, the TomahawkFacesContextWrapper class *parses* the
        // web.xml to retrieve these same filter config params. That is IMO seriously ugly
        // and hopefully will be fixed.

        String param = filterConfig.getInitParameter("uploadMaxFileSize");

        _uploadMaxFileSize = resolveSize(param, _uploadMaxFileSize);

        param = filterConfig.getInitParameter("uploadMaxSize");

        if (param != null)
        {
            _uploadMaxSize = resolveSize(param, _uploadMaxSize);
        }
        else
        {
            //If not set, default to uploadMaxFileSize
            _uploadMaxSize = resolveSize(param, _uploadMaxFileSize);
        }
        
        param = filterConfig.getInitParameter("uploadThresholdSize");

        _uploadThresholdSize = resolveSize(param, _uploadThresholdSize);

        _uploadRepositoryPath = filterConfig.getInitParameter("uploadRepositoryPath");
        
        _cacheFileSizeErrors = getBooleanValue(filterConfig.getInitParameter("cacheFileSizeErrors"), false);

        _servletContext = filterConfig.getServletContext();
        
        filterConfig.getServletContext().setAttribute(EXTENSIONS_FILTER_INITIALIZED, true);
    }

    private int resolveSize(String param, int defaultValue) {
        int numberParam = defaultValue;

        if (param != null) {
            param = param.toLowerCase();
            int factor = 1;
            String number = param;

            if (param.endsWith("g")) {
                factor = 1024 * 1024 * 1024;
                number = param.substring(0, param.length() - 1);
            } else if (param.endsWith("m")) {
                factor = 1024 * 1024;
                number = param.substring(0, param.length() - 1);
            } else if (param.endsWith("k")) {
                factor = 1024;
                number = param.substring(0, param.length() - 1);
            }

            numberParam = Integer.parseInt(number) * factor;
        }
        return numberParam;
    }
    
    private static boolean getBooleanValue(String initParameter, boolean defaultVal)
    {
        if(initParameter == null || initParameter.trim().length()==0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter.equalsIgnoreCase("true"));
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(request.getAttribute(DOFILTER_CALLED)!=null)
        {
            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(DOFILTER_CALLED,"true");

        if (!(response instanceof HttpServletResponse)) {
            //If this is a portlet request, just continue the chaining
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Serve resources
        AddResource addResource;

        try
        {
            addResource = AddResourceFactory.getInstance(httpRequest,_servletContext);
            if( addResource.isResourceUri(_servletContext, httpRequest ) ){
                addResource.serveResource(_servletContext, httpRequest, httpResponse);
                return;
            }
        }
        catch(Throwable th)
        {
            log.error("Exception wile retrieving addResource",th);
            throw new ServletException(th);
        }

        HttpServletRequest extendedRequest = httpRequest;

        // For multipart/form-data requests
        // This is done by TomahawkFacesContextWrapper
        if (JakartaServletFileUpload.isMultipartContent(httpRequest)) {
            extendedRequest = new MultipartRequestWrapper(httpRequest, _uploadMaxFileSize, 
                    _uploadThresholdSize, _uploadRepositoryPath, _uploadMaxSize, _cacheFileSizeErrors);
        }
        
        try
        {
            if (addResource instanceof AddResource2)
            {
                ((AddResource2)addResource).responseStarted(_servletContext, extendedRequest);
            }
            else
            {
                addResource.responseStarted();
            }
            
            //This case is necessary when is used            
            //org.apache.myfaces.renderkit.html.util.DefaultAddResource
            //Buffers the output and add to the header the necessary stuff
            //In other case this is simply ignored (NonBufferingAddResource and
            //StreamingAddResource), because this not require buffering
            //and the chaining continues.
            if (addResource.requiresBuffer())
            {
                ExtensionsResponseWrapper extendedResponse = new ExtensionsResponseWrapper((HttpServletResponse) response);
        
                // Standard request
                chain.doFilter(extendedRequest, extendedResponse);
        
                extendedResponse.finishResponse();
        
                // write the javascript stuff for myfaces and headerInfo, if needed
                HttpServletResponse servletResponse = (HttpServletResponse)response;
        
                // only parse HTML responses
                if (extendedResponse.getContentType() != null && isValidContentType(extendedResponse.getContentType()))
                {
                    addResource.parseResponse(extendedRequest, extendedResponse.toString(),
                            servletResponse);
        
                    addResource.writeMyFacesJavascriptBeforeBodyEnd(extendedRequest,
                            servletResponse);
        
                    if( ! addResource.hasHeaderBeginInfos() ){
                        // writes the response if no header info is needed
                        addResource.writeResponse(extendedRequest, servletResponse);
                        return;
                    }
        
                    // Some headerInfo has to be added
                    addResource.writeWithFullHeader(extendedRequest, servletResponse);
        
                    // writes the response
                    addResource.writeResponse(extendedRequest, servletResponse);
                }
                else
                {

                    byte[] responseArray = extendedResponse.getBytes();

                    if(responseArray.length > 0)
                    {
                        // When not filtering due to not valid content-type, deliver the byte-array instead of a charset-converted string.
                        // Otherwise a binary stream gets corrupted.
                        servletResponse.getOutputStream().write(responseArray);
                    }
                }
            }
            else
            {
                chain.doFilter(extendedRequest, response);
            }
        }
        finally
        {
            addResource.responseFinished();         
        }
        
        //chain.doFilter(extendedRequest, response);
    }

    public boolean isValidContentType(String contentType)
    {
        return contentType.startsWith("text/html") ||
                contentType.startsWith("text/xml") ||
                contentType.startsWith("application/xhtml+xml") ||
                contentType.startsWith("application/xml");
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
        // NoOp
    }


}
