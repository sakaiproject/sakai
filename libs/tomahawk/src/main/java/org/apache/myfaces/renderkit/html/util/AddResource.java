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

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This interface defines methods necessary to render links to resources used
 * by custom components.
 * Mostly used to avoid having to include [script src="..."][/script]
 * in the head of the pages before using a component.
 *
 * @author Sylvain Vieujot (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public interface AddResource
{
    public static final ResourcePosition HEADER_BEGIN = new ResourcePosition(0);

    public static final ResourcePosition BODY_END = new ResourcePosition(1);

    public static final ResourcePosition BODY_ONLOAD = new ResourcePosition(2);

    // Methods to add resources

    /**
     * set the context path of the web-app
     */
    public void setContextPath(String contextPath);

    /**
     * Insert a [script src="url"] entry at the current location in the response.
     * The resource is expected to be in the classpath, at the same location as the
     * specified component + "/resource".
     * <p>
     * Example: when customComponent is class example.Widget, and
     * resourceName is script.js, the resource will be retrieved from
     * "example/Widget/resource/script.js" in the classpath.
     */
    public void addJavaScriptHere(FacesContext context, Class myfacesCustomComponent,
            String resourceName) throws IOException;

    /**
     * Insert a [script src="url"] entry at the current location in the response.
     *
     * @param uri is the location of the desired resource, relative to the base
     * directory of the webapp (ie its contextPath).
     */
    public void addJavaScriptHere(FacesContext context, String uri) throws IOException;

    /**
     * Insert a [script src="url"] entry at the current location in the response.<br />
     * In constrast to the other methods this will not encode the url. So
     * ,depending on the use case, it wont work in cookie-only environments.
     *
     * @param uri is the location of the desired resource, relative to the base
     * directory of the webapp (ie its contextPath).
     * @deprecated just to help to workaround a dojo bug
     */
    public void addJavaScriptHerePlain(FacesContext context, String uri) throws IOException;
    
    /**
     * Insert a [script src="url"] entry at the current location in the response.
     *
     * @param context
     *
     * @param resourceHandler is an object which specifies exactly how to build the url
     * that is emitted into the script tag. Code which needs to generate URLs in ways
     * that this class does not support by default can implement a custom ResourceHandler.
     *
     * @throws IOException
     */
    public void addJavaScriptHere(FacesContext context, ResourceHandler resourceHandler)
            throws IOException;

    public void addResourceHere(FacesContext context, ResourceHandler resourceHandler)
            throws IOException;

    /**
     * Adds the given Javascript resource to the document header at the specified
     * document positioy by supplying a resourcehandler instance.
     * <p>
     * Use this method to have full control about building the reference url
     * to identify the resource and to customize how the resource is
     * written to the response. In most cases, however, one of the convenience
     * methods on this class can be used without requiring a custom ResourceHandler
     * to be provided.
     * <p>
     * If the script has already been referenced, it's added only once.
     * <p>
     * Note that this method <i>queues</i> the javascript for insertion, and that
     * the script is inserted into the buffered response by the ExtensionsFilter
     * after the page is complete.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
            ResourceHandler resourceHandler);

    /**
     * Insert a [script src="url"] entry into the document header at the
     * specified document position. If the script has already been
     * referenced, it's added only once.
     * <p>
     * The resource is expected to be in the classpath, at the same location as the
     * specified component + "/resource".
     * <p>
     * Example: when customComponent is class example.Widget, and
     * resourceName is script.js, the resource will be retrieved from
     * "example/Widget/resource/script.js" in the classpath.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
            Class myfacesCustomComponent, String resourceName);

    /**
     * Insert a [script src="url"] entry into the document header at the
     * specified document position. If the script has already been
     * referenced, it's added only once.
     *
     * @param defer specifies whether the html attribute "defer" is set on the
     * generated script tag. If this is true then the browser will continue
     * processing the html page without waiting for the specified script to
     * load and be run.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
            Class myfacesCustomComponent, String resourceName, boolean defer);

    /**
     * Insert a [script src="url"] entry into the document header at the
     * specified document position. If the script has already been
     * referenced, it's added only once.
     *
     * @param uri is the location of the desired resource, relative to the base
     * directory of the webapp (ie its contextPath).
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, String uri);

    /**
     * Adds the given Javascript resource at the specified document position.
     * If the script has already been referenced, it's added only once.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, String uri,
            boolean defer);

    public void addJavaScriptToBodyTag(FacesContext context, String javascriptEventName,
            String addedJavaScript);

    /**
     * Adds the given Javascript resource at the specified document position.
     * If the script has already been referenced, it's added only once.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
            ResourceHandler resourceHandler, boolean defer);

    /**
     * Adds the given Javascript resource at the specified document position.
     * If the script has already been referenced, it's added only once.<br />
     * In constrast to the other methods this will not encode the url. So
     * ,depending on the use case, it wont work in cookie-only environments.
     * 
     * @deprecated just to help to workaround a dojo bug
     */
    public void addJavaScriptAtPositionPlain(FacesContext context, ResourcePosition position,
            Class myfacesCustomComponent, String resourceName);
    
    /**
     * Adds the given Style Sheet at the specified document position.
     * If the style sheet has already been referenced, it's added only once.
     */
    public void addStyleSheet(FacesContext context, ResourcePosition position,
            Class myfacesCustomComponent, String resourceName);

    /**
     * Adds the given Style Sheet at the specified document position.
     * If the style sheet has already been referenced, it's added only once.
     */
    public void addStyleSheet(FacesContext context, ResourcePosition position, String uri);

    /**
     * Adds the given Style Sheet at the specified document position.
     * If the style sheet has already been referenced, it's added only once.
     */
    public void addStyleSheet(FacesContext context, ResourcePosition position,
            ResourceHandler resourceHandler);

    /**
     * Adds the given Inline Style at the specified document position.
     */
    public void addInlineStyleAtPosition(FacesContext context, ResourcePosition position, String inlineStyle);

    /**
     * Adds the given Inline Script at the specified document position.
     */
    public void addInlineScriptAtPosition(FacesContext context, ResourcePosition position,
            String inlineScript);

    /**
     * Return a URI that can be embedded into an HTML page to reference a resource
     * from a Tomahawk jarfile.
     * <p>
     * This method is intended for internal use by the Tomahawk project only,
     * and will not serve resources for other projects (unless a custom
     * AddResource implementation has been configured). Non-tomahawk code should
     * use the variants that take an explicit ResourceHandler class.
     * <p>
     * Parameter myfacesCustomComponent is a tomahawk class that the resource
     * is associated with. The resource is then expected to be in the classpath
     * in the same package as the specified class (or a subpackage).
     * <p>
     * Parameter resource is a path relative to the .class file of the specified
     * myfacesCustomComponent class.
     * 
     * Param withContextPath controls whether the webapp name is prefixed to
     * the generated url.
     */
    public String getResourceUri(FacesContext context, Class myfacesCustomComponent,
            String resource, boolean withContextPath);

    public String getResourceUri(FacesContext context, Class myfacesCustomComponent, String resource);

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, ResourceHandler resourceHandler);

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, ResourceHandler resourceHandler,
            boolean withContextPath);

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, String uri);

    /**
     * Get the Path used to retrieve an resource.
     */
    public String getResourceUri(FacesContext context, String uri, boolean withContextPath);


    public boolean isResourceUri(ServletContext servletContext, HttpServletRequest request);

    public void serveResource(ServletContext context, HttpServletRequest request,
            HttpServletResponse response) throws IOException;

    /**
     * Parses the response to mark the positions where code will be inserted
     */
    public void parseResponse(HttpServletRequest request, String bufferedResponse,
            HttpServletResponse response) throws IOException;

    /**
     * Writes the javascript code necessary for myfaces in every page, just befode the closing &lt;/body&gt; tag
     */
    public void writeMyFacesJavascriptBeforeBodyEnd(HttpServletRequest request,
            HttpServletResponse response) throws IOException;

    /**
     * Add the resources to the &lt;head&gt; of the page.
     * If the head tag is missing, but the &lt;body&gt; tag is present, the head tag is added.
     * If both are missing, no resource is added.
     *
     * The ordering is such that the user header CSS & JS override the MyFaces' ones.
     */
    public void writeWithFullHeader(HttpServletRequest request,
            HttpServletResponse response) throws IOException;

    /**
     * Writes the response
     */
    public void writeResponse(HttpServletRequest request,
            HttpServletResponse response) throws IOException;

    /**
     * return true if you require the complete response buffered
     */
    public boolean requiresBuffer();

    /**
     * called when the response start
     */
    public void responseStarted();
    
    /**
     * called when the response has finished
     */
    public void responseFinished();

    /**
     * check there is something to write to the header
     */
    public boolean hasHeaderBeginInfos();
}
