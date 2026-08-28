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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is a utility class to render link to resources used by custom components.
 * Mostly used to avoid having to include [script src="..."][/script]
 * in the head of the pages before using a component.
 * <p/>
 * When used together with the ExtensionsFilter, this class can allow components
 * in the body of a page to emit script and stylesheet references into the page
 * head section. The relevant methods on this object simply queue the changes,
 * and when the page is complete the ExtensionsFilter calls back into this
 * class to allow it to insert the commands into the buffered response.
 * <p/>
 * This class also works with the ExtensionsFilter to allow components to
 * emit references to javascript/css/etc which are bundled in the component's
 * jar file. Special URLs are generated which the ExtensionsFilter will later
 * handle by retrieving the specified resource from the classpath.
 * <p/>
 * The special URL format is:
 * <pre>
 * {contextPath}/faces/myFacesExtensionResource/
 *    {resourceLoaderName}/{cacheKey}/{resourceURI}
 * </pre>
 * Where:
 * <ul>
 * <li> {contextPath} is the context path of the current webapp
 * <li> {resourceLoaderName} is the fully-qualified name of a class which
 * implements the ResourceLoader interface. When a browser app sends a request
 * for the specified resource, an instance of the specified ResourceLoader class
 * will be created and passed the resourceURI part of the URL for resolving to the
 * actual resource to be served back. The standard MyFaces ResourceLoader
 * implementation only serves resources for files stored beneath path
 * org/apache/myfaces/custom in the classpath but non-myfaces code can provide their
 * own ResourceLoader implementations.
 * </ul>
 *
 * @author Sylvain Vieujot (latest modification by $Author: mmarinschek $)
 * @version $Revision: 371739 373827 $ $Date: 2006-01-31 14:50:35 +0000 (Tue, 31 Jan 2006) $
 */
public class DefaultAddResource extends NonBufferingAddResource
{
    protected Log log = LogFactory.getLog(DefaultAddResource.class);


    protected StringBuffer originalResponse;

    private Set headerBeginInfo;
    private Set bodyEndInfo;
    private Set bodyOnloadInfo;


    protected boolean parserCalled = false;
    protected int headerInsertPosition = -1;
    protected int bodyInsertPosition = -1;
    protected int beforeBodyPosition = -1;
    protected int afterBodyContentInsertPosition = -1;
    protected int beforeBodyEndPosition = -1;


    protected DefaultAddResource()
    {
    }

    // Methods to add resources

    /**
     * Adds the given Javascript resource to the document header at the specified
     * document positioy by supplying a resourcehandler instance.
     * <p/>
     * Use this method to have full control about building the reference url
     * to identify the resource and to customize how the resource is
     * written to the response. In most cases, however, one of the convenience
     * methods on this class can be used without requiring a custom ResourceHandler
     * to be provided.
     * <p/>
     * If the script has already been referenced, it's added only once.
     * <p/>
     * Note that this method <i>queues</i> the javascript for insertion, and that
     * the script is inserted into the buffered response by the ExtensionsFilter
     * after the page is complete.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
                                        ResourceHandler resourceHandler)
    {
        addJavaScriptAtPosition(context, position, resourceHandler, false);
    }

    /**
     * Insert a [script src="url"] entry into the document header at the
     * specified document position. If the script has already been
     * referenced, it's added only once.
     * <p/>
     * The resource is expected to be in the classpath, at the same location as the
     * specified component + "/resource".
     * <p/>
     * Example: when customComponent is class example.Widget, and
     * resourceName is script.js, the resource will be retrieved from
     * "example/Widget/resource/script.js" in the classpath.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
                                        Class myfacesCustomComponent, String resourceName)
    {
        addJavaScriptAtPosition(context, position, new MyFacesResourceHandler(
                myfacesCustomComponent, resourceName));
    }

    public void addJavaScriptAtPositionPlain(FacesContext context, ResourcePosition position, Class myfacesCustomComponent, String resourceName)
    {
        addJavaScriptAtPosition(context, position,
                new MyFacesResourceHandler(myfacesCustomComponent, resourceName),
                false, false);
    }


    /**
     * Insert a [script src="url"] entry into the document header at the
     * specified document position. If the script has already been
     * referenced, it's added only once.
     *
     * @param defer specifies whether the html attribute "defer" is set on the
     *              generated script tag. If this is true then the browser will continue
     *              processing the html page without waiting for the specified script to
     *              load and be run.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
                                        Class myfacesCustomComponent, String resourceName, boolean defer)
    {
        addJavaScriptAtPosition(context, position, new MyFacesResourceHandler(
                myfacesCustomComponent, resourceName), defer);
    }

    /**
     * Insert a [script src="url"] entry into the document header at the
     * specified document position. If the script has already been
     * referenced, it's added only once.
     *
     * @param uri is the location of the desired resource, relative to the base
     *            directory of the webapp (ie its contextPath).
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, String uri)
    {
        addJavaScriptAtPosition(context, position, uri, false);
    }

    /**
     * Adds the given Javascript resource at the specified document position.
     * If the script has already been referenced, it's added only once.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position, String uri,
                                        boolean defer)
    {
        addPositionedInfo(position, getScriptInstance(context, uri, defer));
    }

    public void addJavaScriptToBodyTag(FacesContext context, String javascriptEventName,
                                       String addedJavaScript)
    {
        AttributeInfo info = new AttributeInfo();
        info.setAttributeName(javascriptEventName);
        info.setAttributeValue(addedJavaScript);

        addPositionedInfo(BODY_ONLOAD, info);
    }

    /**
     * Adds the given Javascript resource at the specified document position.
     * If the script has already been referenced, it's added only once.
     */
    public void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
                                        ResourceHandler resourceHandler, boolean defer)
    {
        validateResourceHandler(resourceHandler);
        addPositionedInfo(position, getScriptInstance(context, resourceHandler, defer));
    }

    private void addJavaScriptAtPosition(FacesContext context, ResourcePosition position,
                                         ResourceHandler resourceHandler, boolean defer, boolean encodeUrl)
    {
        validateResourceHandler(resourceHandler);
        addPositionedInfo(position, getScriptInstance(context, resourceHandler, defer, encodeUrl));
    }

    /**
     * Adds the given Style Sheet at the specified document position.
     * If the style sheet has already been referenced, it's added only once.
     */
    public void addStyleSheet(FacesContext context, ResourcePosition position,
                              Class myfacesCustomComponent, String resourceName)
    {
        addStyleSheet(context, position, new MyFacesResourceHandler(myfacesCustomComponent,
                resourceName));
    }

    /**
     * Adds the given Style Sheet at the specified document position.
     * If the style sheet has already been referenced, it's added only once.
     */
    public void addStyleSheet(FacesContext context, ResourcePosition position, String uri)
    {
        addPositionedInfo(position, getStyleInstance(context, uri));
    }

    /**
     * Adds the given Style Sheet at the specified document position.
     * If the style sheet has already been referenced, it's added only once.
     */
    public void addStyleSheet(FacesContext context, ResourcePosition position,
                              ResourceHandler resourceHandler)
    {
        validateResourceHandler(resourceHandler);
        addPositionedInfo(position, getStyleInstance(context, resourceHandler));
    }

    /**
     * Adds the given Inline Style at the specified document position.
     */
    public void addInlineStyleAtPosition(FacesContext context, ResourcePosition position, String inlineStyle)
    {
        addPositionedInfo(position, getInlineStyleInstance(inlineStyle));
    }

    /**
     * Adds the given Inline Script at the specified document position.
     */
    public void addInlineScriptAtPosition(FacesContext context, ResourcePosition position,
                                          String inlineScript)
    {
        addPositionedInfo(position, getInlineScriptInstance(inlineScript));
    }

    // Positioned stuffs

    protected Set getHeaderBeginInfos()
    {
        if (headerBeginInfo == null)
        {
            headerBeginInfo = new LinkedHashSet();
        }
        return headerBeginInfo;
    }

    protected Set getBodyEndInfos()
    {
        if (bodyEndInfo == null)
        {
            bodyEndInfo = new LinkedHashSet();
        }
        return bodyEndInfo;
    }

    protected Set getBodyOnloadInfos()
    {
        if (bodyOnloadInfo == null)
        {
            bodyOnloadInfo = new LinkedHashSet();
        }
        return bodyOnloadInfo;
    }

    private void addPositionedInfo(ResourcePosition position, PositionedInfo info)
    {
        if (HEADER_BEGIN.equals(position))
        {
            Set set = getHeaderBeginInfos();
            set.add(info);
        }
        else if (BODY_END.equals(position))
        {
            Set set = getBodyEndInfos();
            set.add(info);

        }
        else if (BODY_ONLOAD.equals(position))
        {
            Set set = getBodyOnloadInfos();
            set.add(info);
        }
    }

    public boolean hasHeaderBeginInfos()
    {
        return headerBeginInfo != null;
    }

    /**
     * Parses the response to mark the positions where code will be inserted
     */
    public void parseResponse(HttpServletRequest request, String bufferedResponse,
                              HttpServletResponse response)
    {

        originalResponse = new StringBuffer(bufferedResponse);

        ParseCallbackListener l = new ParseCallbackListener();
        ReducedHTMLParser.parse(originalResponse, l);

        headerInsertPosition = l.getHeaderInsertPosition();
        bodyInsertPosition = l.getBodyInsertPosition();
        beforeBodyPosition = l.getBeforeBodyPosition();
        afterBodyContentInsertPosition = l.getAfterBodyContentInsertPosition();
        beforeBodyEndPosition = l.getAfterBodyEndPosition() - 7;  // 7, which is the length of </body>

        parserCalled = true;
    }

    /**
     * Writes the javascript code necessary for myfaces in every page, just befode the closing &lt;/body&gt; tag
     */
    public void writeMyFacesJavascriptBeforeBodyEnd(HttpServletRequest request,
                                                    HttpServletResponse response) throws IOException
    {
        if (!parserCalled)
        {
            throw new IOException("Method parseResponse has to be called first");
        }

        if (beforeBodyEndPosition >= 0)
        {
            String myFacesJavascript = (String) request.getAttribute("org.apache.myfaces.myFacesJavascript");
            if (myFacesJavascript != null)
            {
                originalResponse.insert(beforeBodyEndPosition, myFacesJavascript);
            }
            else
            {
                log.warn("MyFaces special javascript could not be retrieved from request-map.");
            }
        }
    }

    /**
     * Add the resources to the &lt;head&gt; of the page.
     * If the head tag is missing, but the &lt;body&gt; tag is present, the head tag is added.
     * If both are missing, no resource is added.
     * <p/>
     * The ordering is such that the user header CSS & JS override the MyFaces' ones.
     */
    public void writeWithFullHeader(HttpServletRequest request,
                                    HttpServletResponse response) throws IOException
    {
        if (!parserCalled)
        {
            throw new IOException("Method parseResponse has to be called first");
        }

        boolean addHeaderTags = false;

        if (headerInsertPosition == -1)
        {
            if (beforeBodyPosition != -1)
            {
                // The input html has a body start tag, but no head tags. We therefore
                // need to insert head start/end tags for our content to live in.
                addHeaderTags = true;
                headerInsertPosition = beforeBodyPosition;
            }
            else
            {
                // neither head nor body tags in the input
                log.warn("Response has no <head> or <body> tag:\n" + originalResponse);
            }
        }

        ResponseWriter writer = new HtmlResponseWriterImpl(response.getWriter(),
                HtmlRendererUtils.selectContentType(request.getHeader("accept")),
                response.getCharacterEncoding());

        if (afterBodyContentInsertPosition >= 0)
        {
            // insert all the items that want to go immediately after the <body> tag.
            HtmlBufferResponseWriterWrapper writerWrapper = HtmlBufferResponseWriterWrapper
                    .getInstance(writer);

            for (Iterator i = getBodyEndInfos().iterator(); i.hasNext();)
            {
                writerWrapper.write("\n");

                PositionedInfo positionedInfo = (PositionedInfo) i.next();

                if (!(positionedInfo instanceof WritablePositionedInfo))
                    throw new IllegalStateException("positionedInfo of type : "
                            + positionedInfo.getClass().getName());
                ((WritablePositionedInfo) positionedInfo).writePositionedInfo(response,
                        writerWrapper);
            }

            originalResponse.insert(headerInsertPosition, writerWrapper.toString());
        }

        if (bodyInsertPosition > 0)
        {
            StringBuffer buf = new StringBuffer();
            Set bodyInfos = getBodyOnloadInfos();
            if (bodyInfos.size() > 0)
            {
                int i = 0;
                for (Iterator it = getBodyOnloadInfos().iterator(); it.hasNext();)
                {
                    AttributeInfo positionedInfo = (AttributeInfo) it.next();
                    if (i == 0)
                    {
                        buf.append(positionedInfo.getAttributeName());
                        buf.append("=\"");
                    }
                    buf.append(positionedInfo.getAttributeValue());

                    i++;
                }

                buf.append("\"");
                originalResponse.insert(bodyInsertPosition - 1, " " + buf.toString());
            }
        }

        if (headerInsertPosition >= 0)
        {
            HtmlBufferResponseWriterWrapper writerWrapper = HtmlBufferResponseWriterWrapper
                    .getInstance(writer);

            if (addHeaderTags)
                writerWrapper.write("<head>");

            for (Iterator i = getHeaderBeginInfos().iterator(); i.hasNext();)
            {
                writerWrapper.write("\n");

                PositionedInfo positionedInfo = (PositionedInfo) i.next();

                if (!(positionedInfo instanceof WritablePositionedInfo))
                    throw new IllegalStateException("positionedInfo of type : "
                            + positionedInfo.getClass().getName());
                ((WritablePositionedInfo) positionedInfo).writePositionedInfo(response,
                        writerWrapper);
            }

            if (addHeaderTags)
                writerWrapper.write("</head>");

            originalResponse.insert(headerInsertPosition, writerWrapper.toString());

        }

    }

    /**
     * Writes the response
     */
    public void writeResponse(HttpServletRequest request,
                              HttpServletResponse response) throws IOException
    {
        ResponseWriter writer = new HtmlResponseWriterImpl(response.getWriter(), HtmlRendererUtils
                .selectContentType(request.getHeader("accept")),
                response.getCharacterEncoding());
        writer.write(originalResponse.toString());
    }

    private PositionedInfo getStyleInstance(FacesContext context, ResourceHandler resourceHandler)
    {
        return new StylePositionedInfo(getResourceUri(context, resourceHandler));
    }

    private PositionedInfo getScriptInstance(FacesContext context, ResourceHandler resourceHandler,
                                             boolean defer)
    {
        return new ScriptPositionedInfo(getResourceUri(context, resourceHandler), defer);
    }

    private PositionedInfo getScriptInstance(FacesContext context, ResourceHandler resourceHandler,
                                             boolean defer, boolean encodeURL)
    {
           return new ScriptPositionedInfo(getResourceUri(context, resourceHandler), defer, encodeURL);
    }

    private PositionedInfo getStyleInstance(FacesContext context, String uri)
    {
        return new StylePositionedInfo(getResourceUri(context, uri));
    }

    protected PositionedInfo getScriptInstance(FacesContext context, String uri, boolean defer)
    {
        return new ScriptPositionedInfo(getResourceUri(context, uri), defer);
    }

    private PositionedInfo getInlineScriptInstance(String inlineScript)
    {
        return new InlineScriptPositionedInfo(inlineScript);
    }

    private PositionedInfo getInlineStyleInstance(String inlineStyle)
    {
        return new InlineStylePositionedInfo(inlineStyle);
    }

    protected interface PositionedInfo
    {
    }

    protected static class AttributeInfo implements PositionedInfo
    {
        private String _attributeName;
        private String _attributeValue;

        public String getAttributeName()
        {
            return _attributeName;
        }

        public void setAttributeName(String attributeName)
        {
            _attributeName = attributeName;
        }

        public String getAttributeValue()
        {
            return _attributeValue;
        }

        public void setAttributeValue(String attributeValue)
        {
            _attributeValue = attributeValue;
        }
    }

    protected interface WritablePositionedInfo extends PositionedInfo
    {
        public abstract void writePositionedInfo(HttpServletResponse response, ResponseWriter writer)
                throws IOException;
    }

    private abstract class AbstractResourceUri
    {
        protected final String _resourceUri;

        protected AbstractResourceUri(String resourceUri)
        {
            _resourceUri = resourceUri;
        }

        public int hashCode()
        {
            return _resourceUri.hashCode();
        }

        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof AbstractResourceUri)
            {
                AbstractResourceUri other = (AbstractResourceUri) obj;
                return _resourceUri.equals(other._resourceUri);
            }
            return false;
        }

        protected String getResourceUri()
        {
            return _resourceUri;
        }
    }

    private class StylePositionedInfo extends AbstractResourceUri implements WritablePositionedInfo
    {
        protected StylePositionedInfo(String resourceUri)
        {
            super(resourceUri);
        }

        public void writePositionedInfo(HttpServletResponse response, ResponseWriter writer)
                throws IOException
        {
            writeStyleReference(response, writer, this.getResourceUri());
        }
    }

    private class ScriptPositionedInfo extends AbstractResourceUri implements
            WritablePositionedInfo
    {
        protected final boolean _defer;
        protected final boolean _encode;

        public ScriptPositionedInfo(String resourceUri, boolean defer)
        {
            this(resourceUri, defer, true);
        }

        public ScriptPositionedInfo(String resourceUri, boolean defer, boolean encodeUrl)
        {
            super(resourceUri);
            _defer = defer;
            _encode = encodeUrl;
        }

        public int hashCode()
        {
            return new HashCodeBuilder()
                .append(this.getResourceUri())
                .append(_defer)
                .append(_encode)
                .toHashCode();
        }

        public boolean equals(Object obj)
        {
            if (super.equals(obj))
            {
                if (obj instanceof ScriptPositionedInfo)
                {
                    ScriptPositionedInfo other = (ScriptPositionedInfo) obj;
                    return new EqualsBuilder()
                        .append(_defer, other._defer)
                        .append(_encode, other._encode)
                        .isEquals();
                }
            }
            return false;
        }

        public void writePositionedInfo(HttpServletResponse response, ResponseWriter writer)
                throws IOException
        {
            writeJavaScriptReference(response, writer, this.getResourceUri(),_encode,_defer);
        }
    }

    private abstract class InlinePositionedInfo implements WritablePositionedInfo
    {
        private final String _inlineValue;

        protected InlinePositionedInfo(String inlineValue)
        {
            _inlineValue = inlineValue;
        }

        public String getInlineValue()
        {
            return _inlineValue;
        }

        public int hashCode()
        {
            return new HashCodeBuilder().append(_inlineValue).toHashCode();
        }

        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof InlinePositionedInfo)
            {
                InlinePositionedInfo other = (InlinePositionedInfo) obj;
                return new EqualsBuilder().append(_inlineValue, other._inlineValue).isEquals();
            }
            return false;
        }
    }

    private class InlineScriptPositionedInfo extends InlinePositionedInfo
    {
        protected InlineScriptPositionedInfo(String inlineScript)
        {
            super(inlineScript);
        }

        public void writePositionedInfo(HttpServletResponse response, ResponseWriter writer)
                throws IOException
        {
            writeInlineScript(writer, getInlineValue());
        }        
    }

    private class InlineStylePositionedInfo extends InlinePositionedInfo
    {
        protected InlineStylePositionedInfo(String inlineStyle)
        {
            super(inlineStyle);
        }

        public void writePositionedInfo(HttpServletResponse response, ResponseWriter writer)
                throws IOException
        {
            writeInlineStylesheet(writer, getInlineValue());
        }
    }

    protected static class ParseCallbackListener implements CallbackListener
    {
        private int headerInsertPosition = -1;
        private int bodyInsertPosition = -1;
        private int beforeBodyPosition = -1;
        private int afterBodyContentInsertPosition = -1;
        private int afterBodyEndPosition = -1;

        public ParseCallbackListener()
        {
        }

        public void openedStartTag(int charIndex, int tagIdentifier)
        {
            if (tagIdentifier == ReducedHTMLParser.BODY_TAG)
            {
                beforeBodyPosition = charIndex;
            }
        }

        public void closedStartTag(int charIndex, int tagIdentifier)
        {
            if (tagIdentifier == ReducedHTMLParser.HEAD_TAG)
            {
                headerInsertPosition = charIndex;
            }
            else if (tagIdentifier == ReducedHTMLParser.BODY_TAG)
            {
                bodyInsertPosition = charIndex;
            }
        }

        public void openedEndTag(int charIndex, int tagIdentifier)
        {
            if (tagIdentifier == ReducedHTMLParser.BODY_TAG)
            {
                afterBodyContentInsertPosition = charIndex;
            }
        }

        public void closedEndTag(int charIndex, int tagIdentifier)
        {
            if (tagIdentifier == ReducedHTMLParser.BODY_TAG)
            {
                afterBodyEndPosition = charIndex;
            }
        }

        public void attribute(int charIndex, int tagIdentifier, String key, String value)
        {
        }

        public int getHeaderInsertPosition()
        {
            return headerInsertPosition;
        }

        public int getBodyInsertPosition()
        {
            return bodyInsertPosition;
        }

        public int getBeforeBodyPosition()
        {
            return beforeBodyPosition;
        }

        public int getAfterBodyContentInsertPosition()
        {
            return afterBodyContentInsertPosition;
        }

        public int getAfterBodyEndPosition()
        {
            return afterBodyEndPosition;
        }
    }

    public boolean requiresBuffer()
    {
        return true;
    }

    public void responseStarted()
    {
    }

    public void responseFinished()
    {
    }
}
