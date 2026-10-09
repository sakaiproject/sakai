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
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @since 1.1.7
 * @author Martin Marinschek
 */
public class NonBufferingAddResource implements AddResource {

    protected static final String PATH_SEPARATOR = "/";
    protected String _contextPath;

    private static final String RESOURCES_CACHE_KEY = AddResource.class.getName() + ".CACHE_KEY";

    private String resourceVirtualPath;

    protected Log log = LogFactory.getLog(NonBufferingAddResource.class);

    /**
     * the context path for the web-app.<br />
     * You can set the context path only once, every subsequent set will throw an SecurityException
     */
    public void setContextPath(String contextPath)
    {
        if (_contextPath != null)
        {
            throw new SecurityException("context path already set");
        }

        _contextPath = contextPath;
    }

    // Methods to add resources

    /**
     * Insert a [script src="url"] entry at the current location in the response.
     * The resource is expected to be in the classpath, at the same location as the
     * specified component + "/resource".
     * <p/>
     * Example: when customComponent is class example.Widget, and
     * resourceName is script.js, the resource will be retrieved from
     * "example/Widget/resource/script.js" in the classpath.
     */
    public void addJavaScriptHere(FacesContext context, Class myfacesCustomComponent,
                                  String resourceName) throws IOException
    {
        addJavaScriptHere(context, new MyFacesResourceHandler(myfacesCustomComponent, resourceName));
    }

    /**
     * Insert a [script src="url"] entry at the current location in the response.
     *
     * @param uri is the location of the desired resource, relative to the base
     *            directory of the webapp (ie its contextPath).
     */
    public void addJavaScriptHere(FacesContext context, String uri) throws IOException
    {
        writeJavaScriptReference(context, getResourceUri(context, uri), true, false);
    }

    protected static void writeJavaScriptReference(FacesContext context, String resourceUri, boolean encoding, boolean defer) throws IOException{
        ResponseWriter writer = context.getResponseWriter();

        String src = null;
        if(encoding) {
            src=context.getExternalContext().encodeResourceURL(resourceUri);
        }
        else {
            src = resourceUri;
        }

        writeJavaScriptReference(defer, writer, src);
    }

    protected static void writeJavaScriptReference(HttpServletResponse response, ResponseWriter writer, String resourceUri, boolean encoding, boolean defer) throws IOException {
        String src = null;
        if(encoding) {
            src=response.encodeURL(resourceUri);
        }
        else {
            src = resourceUri;
        }

        writeJavaScriptReference(defer, writer, src);
    }

    private static void writeJavaScriptReference(boolean defer, ResponseWriter writer, String src) throws IOException {
        writer.startElement(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SCRIPT_TYPE_ATTR, org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);

        if(defer) {
            writer.writeAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SCRIPT_ELEM_DEFER_ATTR, "true", null);
        }
        writer.writeURIAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SRC_ATTR, src, null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }

    protected static void writeStyleReference(FacesContext context, String resourceUri) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTML.LINK_ELEM, null);
        writer.writeAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.REL_ATTR, org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.STYLESHEET_VALUE, null);
        writer.writeAttribute(HTML.HREF_ATTR, context.getExternalContext().encodeResourceURL(resourceUri), null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.STYLE_TYPE_TEXT_CSS, null);
        writer.endElement(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.LINK_ELEM);
    }

    protected static void writeStyleReference(HttpServletResponse response, ResponseWriter writer,  String resourceUri) throws IOException {
        writer.startElement(HTML.LINK_ELEM, null);
        writer.writeAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.REL_ATTR, org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.STYLESHEET_VALUE, null);
        writer.writeAttribute(HTML.HREF_ATTR, response.encodeURL(resourceUri), null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.STYLE_TYPE_TEXT_CSS, null);
        writer.endElement(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.LINK_ELEM);
    }

    protected static void writeInlineScript(ResponseWriter writer, String inlineScript) throws IOException {
            writer.startElement(HTML.SCRIPT_ELEM, null);
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
            writer.writeText(inlineScript, null);
            writer.endElement(HTML.SCRIPT_ELEM);
    }

    protected static  void writeInlineStylesheet(ResponseWriter writer, String inlineStyle) throws IOException {
        writer.startElement(HTML.STYLE_ELEM, null);
        writer.writeAttribute(HTML.REL_ATTR, HTML.STYLESHEET_VALUE, null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.STYLE_TYPE_TEXT_CSS, null);
        writer.writeText(inlineStyle, null);
        writer.endElement(HTML.STYLE_ELEM);
    }

    public void addJavaScriptHerePlain(FacesContext context, String uri) throws IOException
    {
        writeJavaScriptReference(context, getResourceUri(context, uri), false, false);
    }

    /**
     * Insert a [script src="url"] entry at the current location in the response.
     *
     * @param context The current faces-context
     * @param resourceHandler is an object which specifies exactly how to build the url
     *                        that is emitted into the script tag. Code which needs to generate URLs in ways
     *                        that this class does not support by default can implement a custom ResourceHandler.
     * @throws IOException
     */
    public void addJavaScriptHere(FacesContext context, ResourceHandler resourceHandler)
            throws IOException
    {
        validateResourceHandler(resourceHandler);
        writeJavaScriptReference(context, getResourceUri(context, resourceHandler), true, false);
    }

    public void addResourceHere(FacesContext context, ResourceHandler resourceHandler)
            throws IOException
    {
        validateResourceHandler(resourceHandler);

        String path = getResourceUri(context, resourceHandler);
        ResponseWriter writer = context.getResponseWriter();
        writer.write(context.getExternalContext().encodeResourceURL(path));
    }

    /**
     * Verify that the resource handler is acceptable. Null is not
     * valid, and the getResourceLoaderClass method must return a
     * Class object whose instances implements the ResourceLoader
     * interface.
     *
     * @param resourceHandler handler to check
     */
    protected void validateResourceHandler(ResourceHandler resourceHandler)
    {
        if (resourceHandler == null)
        {
            throw new IllegalArgumentException("ResourceHandler is null");
        }
        validateResourceLoader(resourceHandler.getResourceLoaderClass());
    }

    /**
     * Given a Class object, verify that the instances of that class
     * implement the ResourceLoader interface.
     *
     * @param resourceloader loader to check
     */
    protected void validateResourceLoader(Class resourceloader)
    {
        if (!ResourceLoader.class.isAssignableFrom(resourceloader))
        {
            throw new FacesException("Class " + resourceloader.getName() + " must implement "
                    + ResourceLoader.class.getName());
        }
    }

    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, ResourceHandler resourceHandler) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,resourceHandler),true,false);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, Class myfacesCustomComponent, String resourceName) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,new MyFacesResourceHandler(
                    myfacesCustomComponent, resourceName)),true,false);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, Class myfacesCustomComponent, String resourceName, boolean defer) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,new MyFacesResourceHandler(
                    myfacesCustomComponent, resourceName)),true,true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, String uri) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,uri),true,false);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, String uri, boolean defer) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,uri),true,true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param context
     * @param javascriptEventName
     * @param addedJavaScript
     *
     * @deprecated
     */
    public void addJavaScriptToBodyTag(FacesContext context, String javascriptEventName, String addedJavaScript) {
        throw new UnsupportedOperationException("not supported anymore - use javascript to register your body-event-handler directly");
    }

    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, ResourceHandler resourceHandler, boolean defer) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,resourceHandler),true,defer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addJavaScriptAtPositionPlain(FacesContext context, ResourcePosition position, Class myfacesCustomComponent, String resourceName) {
        try {
            writeJavaScriptReference(context,getResourceUri(context,new MyFacesResourceHandler(myfacesCustomComponent, resourceName)),false,false);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addStyleSheet(FacesContext context, ResourcePosition position, Class myfacesCustomComponent, String resourceName) {
        try {
            writeStyleReference(context,getResourceUri(context,new MyFacesResourceHandler(myfacesCustomComponent, resourceName)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addStyleSheet(FacesContext context, ResourcePosition position, String uri) {
        try {
            writeStyleReference(context,getResourceUri(context,uri));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addStyleSheet(FacesContext context, ResourcePosition position, ResourceHandler resourceHandler) {
        try {
            writeStyleReference(context,getResourceUri(context,resourceHandler));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addInlineStyleAtPosition(FacesContext context, ResourcePosition position, String inlineStyle) {
        try {
            writeInlineStylesheet(context.getResponseWriter(), inlineStyle);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addInlineScriptAtPosition(FacesContext context, ResourcePosition position, String inlineScript) {
        try {
            writeInlineScript(context.getResponseWriter(), inlineScript);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getResourceUri(FacesContext context, Class myfacesCustomComponent,
                                 String resource, boolean withContextPath)
    {
        return getResourceUri(context,
                new MyFacesResourceHandler(myfacesCustomComponent, resource), withContextPath);
    }

    public String getResourceUri(FacesContext context, Class myfacesCustomComponent, String resource)
    {
        return getResourceUri(context, new MyFacesResourceHandler(myfacesCustomComponent, resource));
    }


    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, ResourceHandler resourceHandler)
    {
        String uri = resourceHandler.getResourceUri(context);
        if (uri == null)
        {
            return getResourceUri(context, resourceHandler.getResourceLoaderClass(), true);
        }
        return getResourceUri(context, resourceHandler.getResourceLoaderClass(), true) + uri;
    }

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, ResourceHandler resourceHandler,
                                 boolean withContextPath)
    {
        String uri = resourceHandler.getResourceUri(context);
        if (uri == null)
        {
            return getResourceUri(context, resourceHandler.getResourceLoaderClass(),
                    withContextPath);
        }
        return getResourceUri(context, resourceHandler.getResourceLoaderClass(), withContextPath)
                + uri;
    }

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, String uri)
    {
        return getResourceUri(context, uri, true);
    }

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, String uri, boolean withContextPath)
    {
        if (withContextPath)
        {
            return context.getApplication().getViewHandler().getResourceURL(context, uri);
        }
        return uri;
    }

    /**
     * Get the Path used to retrieve an resource.
     * @param context current faces-context
     * @param resourceLoader resourceLoader
     * @param withContextPath use the context-path of the web-app when accessing the resources
     *
     * @return the URI of the resource
     */
    protected String getResourceUri(FacesContext context, Class resourceLoader,
                                    boolean withContextPath)
    {
        StringBuffer sb = new StringBuffer(200);
        sb.append(MyfacesConfig.getCurrentInstance(context.getExternalContext()).getResourceVirtualPath());
        sb.append(PATH_SEPARATOR);
        sb.append(resourceLoader.getName());
        sb.append(PATH_SEPARATOR);
        sb.append(getCacheKey(context));
        sb.append(PATH_SEPARATOR);
        return getResourceUri(context, sb.toString(), withContextPath);
    }

    /**
     * Return a value used in the {cacheKey} part of a generated URL for a
     * resource reference.
     * <p/>
     * Caching in browsers normally works by having files served to them
     * include last-modified and expiry-time http headers. Until the expiry
     * time is reached, a browser will silently use its cached version. After
     * the expiry time, it will send a "get if modified since {time}" message,
     * where {time} is the last-modified header from the version it has cached.
     * <p/>
     * Unfortunately this scheme only works well for resources represented as
     * plain files on disk, where the webserver can easily and efficiently see
     * the last-modified time of the resource file. When that query has to be
     * processed by a servlet that doesn't scale well, even when it is possible
     * to determine the resource's last-modified date from servlet code.
     * <p/>
     * Fortunately, for the AddResource class a static resource is only ever
     * accessed because a URL was embedded by this class in a dynamic page.
     * This makes it possible to implement caching by instead marking every
     * resource served with a very long expiry time, but forcing the URL that
     * points to the resource to change whenever the old cached version becomes
     * invalid; the browser effectively thinks it is fetching a different
     * resource that it hasn't seen before. This is implemented by embedding
     * a "cache key" in the generated URL.
     * <p/>
     * Rather than using the actual modification date of a resource as the
     * cache key, we simply use the webapp deployment time. This means that all
     * data cached by browsers will become invalid after a webapp deploy (all
     * the urls to the resources change). It also means that changes that occur
     * to a resource <i>without</i> a webapp redeploy will not be seen by browsers.
     *
     * @param context the current faces-context
     *
     * @return the key for caching
     */
    protected long getCacheKey(FacesContext context)
    {
        // cache key is hold in application scope so it is recreated on redeploying the webapp.
        Map applicationMap = context.getExternalContext().getApplicationMap();
        Long cacheKey = (Long) applicationMap.get(RESOURCES_CACHE_KEY);
        if (cacheKey == null)
        {
            cacheKey = new Long(System.currentTimeMillis() / 100000);
            applicationMap.put(RESOURCES_CACHE_KEY, cacheKey);
        }
        return cacheKey.longValue();
    }

    public boolean isResourceUri(ServletContext servletContext, HttpServletRequest request)
    {

        String path;
        if (_contextPath != null)
        {
            path = _contextPath + getResourceVirtualPath(servletContext);
        }
        else
        {
            path = getResourceVirtualPath(servletContext);
        }

        //fix for TOMAHAWK-660; to be sure this fix is backwards compatible, the
        //encoded context-path is only used as a first option to check for the prefix
        //if we're sure this works for all cases, we can directly return the first value
        //and not double-check.
        try
        {
            if(request.getRequestURI().startsWith(URLEncoder.encode(path,"UTF-8")))
                return true;
        }
        catch (UnsupportedEncodingException e)
        {
            log.error("Unsupported encoding UTF-8 used",e);

        }

        return request.getRequestURI().startsWith(path);
    }

    private String getResourceVirtualPath(ServletContext servletContext)
    {
        if(resourceVirtualPath == null)
        {
            resourceVirtualPath = servletContext.getInitParameter(MyfacesConfig.INIT_PARAM_RESOURCE_VIRTUAL_PATH);

            if(resourceVirtualPath == null)
            {
                resourceVirtualPath = MyfacesConfig.INIT_PARAM_RESOURCE_VIRTUAL_PATH_DEFAULT;
            }
        }

        return resourceVirtualPath;
    }

    private Class getClass(String className) throws ClassNotFoundException
    {
        Class clazz = ClassUtils.classForName(className);
        validateResourceLoader(clazz);
        return clazz;
    }

    public void serveResource(ServletContext context, HttpServletRequest request,
                              HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        String uri = request.getContextPath() + request.getServletPath()
                + (pathInfo == null ? "" : pathInfo);
        String classNameStartsAfter = getResourceVirtualPath(context) + '/';

        int posStartClassName = uri.indexOf(classNameStartsAfter) + classNameStartsAfter.length();
        int posEndClassName = uri.indexOf(PATH_SEPARATOR, posStartClassName);
        String className = uri.substring(posStartClassName, posEndClassName);
        int posEndCacheKey = uri.indexOf(PATH_SEPARATOR, posEndClassName + 1);
        String resourceUri = null;
        if (posEndCacheKey + 1 < uri.length())
        {
            resourceUri = uri.substring(posEndCacheKey + 1);
        }
        try
        {
            Class resourceLoader = getClass(className);
            validateResourceLoader(resourceLoader);
            ((ResourceLoader) resourceLoader.newInstance()).serveResource(context, request,
                    response, resourceUri);

            // Do not call response.flushBuffer buffer here. There is no point, as if there
            // ever were header data to write, this would fail as we have already written
            // the response body. The only point would be to flush the output stream, but
            // that will happen anyway when the servlet container closes the socket.
            //
            // In addition, flushing could fail here; it appears that Microsoft IE
            // hasthe habit of hard-closing its socket as soon as it has received a complete
            // gif file, rather than letting the server close it. The container will hopefully
            // silently ignore exceptions on close.
        }
        catch (ResourceLoader.ClosedSocketException e)
        {
            // The ResourceLoader was unable to send the data because the client closed
            // the socket on us; just ignore.
        }
        catch (ClassNotFoundException e)
        {
            log.error("Could not find class for name: " + className, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Could not find resourceloader class for name: " + className);
        }
        catch (InstantiationException e)
        {
            log.error("Could not instantiate class for name: " + className, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not instantiate resourceloader class for name: " + className);
        }
        catch (IllegalAccessException e)
        {
            log.error("Could not access class for name: " + className, e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Could not access resourceloader class for name: " + className);
        }
        catch (Throwable e)
        {
            log.error("Error while serving resource: " + resourceUri + ", message : " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void parseResponse(HttpServletRequest request, String bufferedResponse, HttpServletResponse response) throws IOException {
        throw new UnsupportedOperationException("non-buffering add resource is not buffering.");
    }

    public void writeMyFacesJavascriptBeforeBodyEnd(HttpServletRequest request, HttpServletResponse response) throws IOException {
        throw new UnsupportedOperationException("non-buffering add resource is not buffering.");
    }

    public void writeWithFullHeader(HttpServletRequest request, HttpServletResponse response) throws IOException {
        throw new UnsupportedOperationException("non-buffering add resource is not buffering.");
    }

    public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        throw new UnsupportedOperationException("non-buffering add resource is not buffering.");
    }

    public boolean requiresBuffer() {
        return false;
    }

    public void responseStarted() {
    }

    public void responseFinished() {
    }

    public boolean hasHeaderBeginInfos() {
        return false;
    }
}
