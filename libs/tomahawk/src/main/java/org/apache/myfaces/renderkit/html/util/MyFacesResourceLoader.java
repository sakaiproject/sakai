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
package org.apache.myfaces.renderkit.html.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.net.HttpURLConnection;

/**
 * A ResourceLoader capable of fetching resources from the classpath,
 * but only for classes under package org.apache.myfaces.custom.
 * <p>
 * The URI is expected to contain two pieces of information: the
 * tomahawk class the resource is associated with, and a relative path
 * from that class to the resource.
 *
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MyFacesResourceLoader implements ResourceLoader
{
    protected static final Log log = LogFactory.getLog(MyFacesResourceLoader.class);

    static final String ORG_APACHE_MYFACES_CUSTOM = "org.apache.myfaces.custom";

    private static long lastModified = 0;

    /**
     * Get the last-modified time of the resource.
     * <p>
     * Unfortunately this is not possible with files inside jars. Instead, the
     * MyFaces build process ensures that there is a file AddResource.properties
     * which has the datestamp of the time the build process was run. This method
     * simply gets that value and returns it.
     * <p>
     * Note that this method is not related to the generation of "cache key"
     * values by the AddResource class, nor does it affect the caching behaviour
     * of web browsers. This value simply goes into the http headers as the
     * last-modified time of the specified resource.
     */
    private static long getLastModified()
    {
        if (lastModified == 0)
        {
            final String format = "yyyy-MM-dd HH:mm:ss Z"; // Must match the one used in the build file
            final String bundleName = AddResource.class.getName();
            ResourceBundle resources = ResourceBundle.getBundle(bundleName);
            String sLastModified = resources.getString("lastModified");
            try
            {
                lastModified = new SimpleDateFormat(format).parse(sLastModified).getTime();
            }
            catch (ParseException e)
            {
                lastModified = new Date().getTime();
                log.warn("Unparsable lastModified : " + sLastModified);
            }
        }

        return lastModified;
    }

    /**
     * Given a URI of form "{partial.class.name}/{resourceName}", locate the
     * specified file within the current classpath and write it to the
     * response object.
     * <p>
     * The partial class name has "org.apache.myfaces.custom." prepended
     * to it to form the fully qualified classname. This class object is
     * loaded, and Class.getResourceAsStream is called on it, passing
     * a uri of "resource/" + {resourceName}.
     * <p>
     * The data written to the response stream includes http headers
     * which define the mime content-type; this is deduced from the
     * filename suffix of the resource.
     * <p>
     * @see org.apache.myfaces.renderkit.html.util.ResourceLoader#serveResource(jakarta.servlet.ServletContext,
     *     jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.String)
     */
    public void serveResource(ServletContext context, HttpServletRequest request,
            HttpServletResponse response, String resourceUri) throws IOException
    {
        String[] uriParts = resourceUri.split("/", 2);

        String component = uriParts[0];
        if (component == null || component.trim().length() == 0)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            log.error("Could not find parameter for component to load a resource.");
            return;
        }
        Class componentClass;
        String className = ORG_APACHE_MYFACES_CUSTOM + "." + component;
        try
        {
            componentClass = loadComponentClass(className);
        }
        catch (ClassNotFoundException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            log.error("Could not find the class for component " + className
                    + " to load a resource.");
            return;
        }
        String resource = uriParts[1];
        if (resource == null || resource.trim().length() == 0)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No resource defined");
            log.error("No resource defined component class " + className);
            return;
        }

        InputStream is = null;

        try
        {
            ResourceProvider resourceProvider;
            if (ResourceProvider.class.isAssignableFrom(componentClass))
            {
                try
                {
                    resourceProvider = (ResourceProvider) componentClass.newInstance();
                }
                catch (InstantiationException e)
                {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to instantiate resource provider for resource "
                            + resource + " for component " + component);
                    log.error("Unable to instantiate resource provider for resource " + resource + " for component " + component, e);
                    return;
                }
                catch (IllegalAccessException e)
                {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to instantiate resource provider for resource "
                            + resource + " for component " + component);
                    log.error("Unable to instantiate resource provider for resource " + resource + " for component " + component, e);
                    return;
                }
            }
            else
            {
                resourceProvider = new DefaultResourceProvider(componentClass);
            }

            if (!resourceProvider.exists(context, resource))
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to find resource "
                        + resource + " for component " + component
                        + ". Check that this file is available " + "in the classpath in sub-directory "
                        + "/resource of the package-directory.");
                log.error("Unable to find resource " + resource + " for component " + component
                        + ". Check that this file is available " + "in the classpath in sub-directory "
                        + "/resource of the package-directory.");
            }
            else
            {
                // URLConnection con = url.openConnection();

                long lastModified = resourceProvider.getLastModified(context, resource);
                if (lastModified < 1)
                {
                    // fallback
                    lastModified = getLastModified();
                }

                long browserDate = request.getDateHeader("If-Modified-Since");
                if (browserDate > -1)
                {
                    // normalize to seconds - this should work with any os
                    lastModified = (lastModified / 1000) * 1000;
                    browserDate = (browserDate / 1000) * 1000;

                    if (lastModified == browserDate)
                    {
                        // the browser already has the correct version

                        response.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                        return;
                    }
                }


                int contentLength = resourceProvider.getContentLength(context, resource);
                String contentEncoding = resourceProvider.getEncoding(context, resource);

                is = resourceProvider.getInputStream(context, resource);

                defineContentHeaders(request, response, resource, contentLength, contentEncoding);
                defineCaching(request, response, resource, lastModified);
                writeResource(request, response, is);
            }
        }
        finally
        {
            // nothing to do here..
        }
    }

    /**
     * Copy the content of the specified input stream to the servlet response.
     */
    protected void writeResource(HttpServletRequest request, HttpServletResponse response,
            InputStream in) throws IOException
    {
        ServletOutputStream out = response.getOutputStream();
        try
        {
            byte[] buffer = new byte[1024];
            for (int size = in.read(buffer); size != -1; size = in.read(buffer))
            {
                out.write(buffer, 0, size);
            }
            out.flush();
        }
        catch(IOException e)
        {
            // This happens sometimes with Microsft Internet Explorer. It would
            // appear (guess) that when javascript creates multiple dom nodes
            // referring to the same remote resource then IE stupidly opens 
            // multiple sockets and requests that resource multiple times. But
            // when the first request completes, it then realises its stupidity
            // and forcibly closes all the other sockets. But here we are trying
            // to service those requests, and so get a "broken pipe" failure 
            // on write. The only thing to do here is to silently ignore the issue,
            // ie suppress the exception. Note that it is also possible for the
            // above code to succeed (ie this exception clause is not run) but
            // for a later flush to get the "broken pipe"; this is either due
            // just to timing, or possibly IE is closing sockets after receiving
            // a complete file for some types (gif?) rather than waiting for the
            // server to close it. We throw a special exception here to inform
            // callers that they should NOT flush anything - though that is
            // dangerous no matter what IOException subclass is thrown.
            log.debug("Unable to send resource data to client", e);
            throw new ResourceLoader.ClosedSocketException();
        }
    }

    /**
     * Output http headers telling the browser (and possibly intermediate caches) how
     * to cache this data.
     * <p>
     * The expiry time in this header info is set to 7 days. This is not a problem as
     * the overall URI contains a "cache key" that changes whenever the webapp is
     * redeployed (see AddResource.getCacheKey), meaning that all browsers will
     * effectively reload files on webapp redeploy.
     */
    protected void defineCaching(HttpServletRequest request, HttpServletResponse response,
            String resource, long lastModified)
    {
        response.setDateHeader("Last-Modified", lastModified);

        Calendar expires = Calendar.getInstance();
        expires.add(Calendar.DAY_OF_YEAR, 7);
        response.setDateHeader("Expires", expires.getTimeInMillis());

        //12 hours: 43200 = 60s * 60 * 12
        response.setHeader("Cache-Control", "max-age=43200");
        response.setHeader("Pragma", "");
    }

    /**
     * Output http headers indicating the mime-type of the content being served.
     * The mime-type output is determined by the resource filename suffix.
     */
    protected void defineContentHeaders(HttpServletRequest request, HttpServletResponse response,
                                        String resource, int contentLength, String contentEncoding)
    {
        String charset = "";
        if (contentEncoding != null)
        {
            charset = "; charset=" + contentEncoding;
        }
        if (contentLength > -1)
        {
            response.setContentLength(contentLength);
        }

        if (resource.endsWith(".js"))
            response.setContentType(
                org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT + charset);
        else if (resource.endsWith(".css"))
            response.setContentType(
                org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.STYLE_TYPE_TEXT_CSS + charset);
        else if (resource.endsWith(".gif"))
            response.setContentType("image/gif");
        else if (resource.endsWith(".png"))
            response.setContentType("image/png");
        else if (resource.endsWith(".jpg") || resource.endsWith(".jpeg"))
            response.setContentType("image/jpeg");
        else if (resource.endsWith(".xml") || resource.endsWith(".xsl"))
            response.setContentType("text/xml"); // XSL has to be served as XML.
    }

    protected Class loadComponentClass(String componentClass) throws ClassNotFoundException
    {
        return ClassUtils.classForName(componentClass);
    }

    // NOTE: This method is not being used. Perhaps it can be removed?
    protected void validateCustomComponent(Class myfacesCustomComponent)
    {
        if (!myfacesCustomComponent.getName().startsWith(ORG_APACHE_MYFACES_CUSTOM + "."))
        {
            throw new IllegalArgumentException(
                    "expected a myfaces custom component class in package "
                            + ORG_APACHE_MYFACES_CUSTOM);
        }
    }
}