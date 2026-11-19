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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResource2;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.tomahawk.util.ExternalContextUtils;
import org.apache.myfaces.webapp.filter.portlet.PortletExternalContextWrapper;
import org.apache.myfaces.webapp.filter.servlet.ServletExternalContextWrapper;

/**
 * This class acts as an alternative to ExtensionsFilter feature.
 * <p>
 * It wraps the FacesContext using TomahawkFacesContextFactory. See this
 * class for parameters and additional information
 * </p>
 * <p>
 * If ExtensionsFilter is used on servlet environment, this wrapper is 
 * not used. You can set this wrapper using the following configuration:
 * <code>
 *   <context-param>
 *     <param-name>org.apache.myfaces.CHECK_EXTENSIONS_FILTER</param-name>
 *     <param-value>false</param-value>
 *   </context-param>
 * 
 *   <context-param>
 *     <param-name>org.apache.myfaces.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER</param-name>
 *     <param-value>false</param-value>
 *   </context-param>
 * </code>
 * </p>
 * <p>
 * Remember map the FacesServet to the org.apache.myfaces.RESOURCE_VIRTUAL_PATH
 * value (default /faces/myFacesExtensionResource, so map FacesServlet to /faces/* 
 * could be used or better /faces/myFacesExtensionResource/*) so the
 * ServeResourcePhaseListener can serve resources.
 * </p>
 * <p>
 * One use that has this wrapper is in portlets (there is no PortletFilter
 * on portlet api 1.0, so to take all tomahawk advantages (components
 * that uses some javascript handled by AddResource api and fileupload
 * support) users must configure this alternative.
 * </p>
 * <p>
 * When it is used this alternative, the params used to configure
 * MultipartRequestWrapper (file upload support) are set using this
 * web.xml config params: 
 * </p>
 * <ul>
 * <li>org.apache.myfaces.UPLOAD_MAX_FILE_SIZE</li>
 * <li>org.apache.myfaces.UPLOAD_THRESHOLD_SIZE</li>
 * <li>org.apache.myfaces.UPLOAD_MAX_REPOSITORY_PATH</li>
 * <li>org.apache.myfaces.UPLOAD_MAX_SIZE</li>
 * <li>org.apache.myfaces.UPLOAD_CACHE_FILE_SIZE_ERRORS</li>
 * </ul>
 * 
 * @since 1.1.7
 * @author Martin Marinschek
 */
public class TomahawkFacesContextWrapper extends FacesContext {

    private FacesContext delegate=null;
    private ExternalContext externalContextDelegate=null;
    private ExtensionsResponseWrapper extensionsResponseWrapper = null;

    public TomahawkFacesContextWrapper(FacesContext delegate)
    {
        this(delegate, null);
    }
    
    public TomahawkFacesContextWrapper(FacesContext delegate, 
            ExtensionsResponseWrapper extensionsResponseWrapper ) {

        this.delegate = delegate;
        
        if(ExternalContextUtils.getRequestType(delegate.getExternalContext()).isPortlet()) {
            
            Object portletResponse = delegate.getExternalContext().getResponse();
            Object portletRequest = delegate.getExternalContext().getRequest();

            Object extendedRequest = portletRequest;
            Object extendedResponse = portletResponse;
            
            // For multipart/form-data requests we need to encapsulate
            // the request using PortletMultipartRequestWrapper. This could not be
            // done on TomahawkFacesContextFactory.getFacesContext(...)
            // because we need an ExternalContext instance to get
            // the init params for the ExtensionsFilter and initialize
            // MultipartRequestWrapper with the correct values.

            // Portlet multipart upload is unsupported in the Sakai fork: commons-fileupload2
            // ships no portlet variant. Servlet multipart handling below is unaffected.
            boolean multipartContent = false;
            
            AddResource addResource= AddResourceFactory.getInstance(this);
            
            if (addResource instanceof AddResource2)
            {
                ((AddResource2)addResource).responseStarted(delegate.getExternalContext().getContext(), extendedRequest);
            }
            else
            {
                addResource.responseStarted();
            }

            if (addResource.requiresBuffer())
            {
                throw new IllegalStateException("buffering not supported in the portal environment. "+
                        " Use for org.apache.myfaces.ADD_RESOURCE_CLASS the value"+
                        " org.apache.myfaces.renderkit.html.util.NonBufferingAddResource.");
            }
            
            externalContextDelegate = new PortletExternalContextWrapper(
                    delegate.getExternalContext(), extendedRequest, extendedResponse, multipartContent);
        }
        else {
            HttpServletResponse httpResponse = (HttpServletResponse) delegate.getExternalContext().getResponse();
            HttpServletRequest httpRequest = (HttpServletRequest) delegate.getExternalContext().getRequest();

            HttpServletRequest extendedRequest = httpRequest;

            // For multipart/form-data requests we need to encapsulate
            // the request using MultipartRequestWrapper. This could not be
            // done on TomahawkFacesContextFactory.getFacesContext(...)
            // because we need an ExternalContext instance to get
            // the init params for the ExtensionsFilter and initialize
            // MultipartRequestWrapper with the correct values.
            
            boolean multipartContent = false;
           
            if (JakartaServletFileUpload.isMultipartContent(httpRequest)) {
                multipartContent = true;
                
                MultipartRequestWrapperConfig config = MultipartRequestWrapperConfig
                        .getMultipartRequestWrapperConfig(delegate
                                .getExternalContext());                
                
                extendedRequest = new MultipartRequestWrapper(httpRequest, config.getUploadMaxFileSize(),
                        config.getUploadThresholdSize(), config.getUploadRepositoryPath(), 
                        config.getUploadMaxSize(), config.isCacheFileSizeErrors());
                
            }

            AddResource addResource= AddResourceFactory.getInstance(this);
            
            if (addResource instanceof AddResource2)
            {
                ((AddResource2)addResource).responseStarted(delegate.getExternalContext().getContext(), extendedRequest);
            }
            else
            {
                addResource.responseStarted();
            }

            if (addResource.requiresBuffer() && extensionsResponseWrapper != null)
            {
                //If the request requires buffer, this was already
                //wrapped (on TomahawkFacesContextFactory.getFacesContext(...) ),
                //but we need to save the wrapped response value 
                //on a local variable to then reference it on release() 
                //method and parse the old response.
                this.extensionsResponseWrapper = (ExtensionsResponseWrapper) extensionsResponseWrapper; 
            }

            externalContextDelegate = new ServletExternalContextWrapper(
                    delegate.getExternalContext(), extendedRequest, httpResponse, multipartContent);            
        }
    }
    
    /**
     * This method uses reflection to call the method of the delegated
     * FacesContext getELContext, present on 1.2. This should be done
     * since we need compatibility between 1.1 and 1.2 for tomahawk
     * 
     * @return
     */
    public jakarta.el.ELContext getELContext() {
        ;
        try
        {
            return (jakarta.el.ELContext) delegate.getClass().getMethod("getELContext", null).invoke(delegate, null);
        }
        catch (IllegalArgumentException e)
        {
            //If the method is called with jsf 1.2, this should not 
            //be thrown
            throw new RuntimeException(e);
        }
        catch (SecurityException e)
        {
            //If the method is called with jsf 1.2, this should not 
            //be thrown
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            //If the method is called with jsf 1.2, this should not 
            //be thrown
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (InvocationTargetException e)
        {
            //If the method is called with jsf 1.2, this should not 
            //be thrown
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (NoSuchMethodException e)
        {
            //If the method is called with jsf 1.2, this should not 
            //be thrown
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());            
        }
    }

    public Application getApplication() {
        return delegate.getApplication();
    }

    public Iterator getClientIdsWithMessages() {
        return delegate.getClientIdsWithMessages();
    }

    public ExternalContext getExternalContext() {
        return externalContextDelegate==null?delegate.getExternalContext():externalContextDelegate;
    }

    public FacesMessage.Severity getMaximumSeverity() {
        return delegate.getMaximumSeverity();
    }

    public Iterator getMessages() {
        return delegate.getMessages();
    }

    public Iterator getMessages(String clientId) {
        return delegate.getMessages(clientId);
    }

    public RenderKit getRenderKit() {
        return delegate.getRenderKit();
    }

    public boolean getRenderResponse() {
        return delegate.getRenderResponse();
    }

    public boolean getResponseComplete() {
        return delegate.getResponseComplete();
    }

    public ResponseStream getResponseStream() {
        return delegate.getResponseStream();
    }

    public void setResponseStream(ResponseStream responseStream) {
        delegate.setResponseStream(responseStream);
    }

    public ResponseWriter getResponseWriter() {
        return delegate.getResponseWriter();
    }

    public void setResponseWriter(ResponseWriter responseWriter) {
        delegate.setResponseWriter(responseWriter);
    }

    public UIViewRoot getViewRoot() {
        return delegate.getViewRoot();
    }

    public void setViewRoot(UIViewRoot root) {
        delegate.setViewRoot(root);
    }

    public void addMessage(String clientId, FacesMessage message) {
        delegate.addMessage(clientId, message);
    }

    public void release() {

        AddResource addResource=null;

        try
        {
            addResource= AddResourceFactory.getInstance(this);
            if (addResource.requiresBuffer())
            {
                if(extensionsResponseWrapper == null) {
                    throw new NullPointerException("When wrapping the faces-context, add-resource told us that no buffer is required, " +
                            "now it is required, and we have a null-extensionsResponseWrapper. Please fix add-resource to be consistent over a single request.");
                }
                extensionsResponseWrapper.finishResponse();

                // write the javascript stuff for myfaces and headerInfo, if needed
                HttpServletResponse servletResponse = extensionsResponseWrapper.getDelegate();
                HttpServletRequest servletRequest = (HttpServletRequest) getExternalContext().getRequest();

                String contentType = extensionsResponseWrapper.getContentType();
                
                // only parse HTML responses
                if (contentType != null && isValidContentType(contentType))
                {
                    String oldResponse = extensionsResponseWrapper.toString();
                    addResource.parseResponse(servletRequest, extensionsResponseWrapper.toString(),
                            servletResponse);

                    addResource.writeMyFacesJavascriptBeforeBodyEnd(servletRequest,
                            servletResponse);

                    if( ! addResource.hasHeaderBeginInfos() ){
                        // writes the response if no header info is needed
                        addResource.writeResponse(servletRequest, servletResponse);
                        return;
                    }

                    // Some headerInfo has to be added
                    addResource.writeWithFullHeader(servletRequest, servletResponse);

                    // writes the response
                    addResource.writeResponse(servletRequest, servletResponse);
                }
                else
                {

                    byte[] responseArray = extensionsResponseWrapper.getBytes();

                    if(responseArray.length > 0)
                    {
                         // When not filtering due to not valid content-type, deliver the byte-array instead of a charset-converted string.
                         // Otherwise a binary stream gets corrupted.
                         servletResponse.getOutputStream().write(responseArray);
                     }
                }
            }
        }
        catch(Throwable th) {
            throw new FacesException(th);
        }
        finally
        {
            try
            {
                if(addResource!=null)
                {
                    addResource.responseFinished();
                }
            }
            finally
            {
                delegate.release();
            }
        }

    }

    public boolean isValidContentType(String contentType)
    {
        return contentType.startsWith("text/html") ||
                contentType.startsWith("text/xml") ||
                contentType.startsWith("application/xhtml+xml") ||
                contentType.startsWith("application/xml");
    }

    public void renderResponse() {
        delegate.renderResponse();
    }

    public void responseComplete() {
        delegate.responseComplete();
    }
    
    /*This method is for do not break MyfacesGenericPortlet*/
    public void setExternalContext(ExternalContext extContext){
        try
        {
            Method method = delegate.getClass().getMethod("setExternalContext", new Class[]{Class.forName("org.apache.myfaces.context.ReleaseableExternalContext")});
            method.invoke(delegate, new Object[]{extContext});
            FacesContext.setCurrentInstance(this);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }
    
}
