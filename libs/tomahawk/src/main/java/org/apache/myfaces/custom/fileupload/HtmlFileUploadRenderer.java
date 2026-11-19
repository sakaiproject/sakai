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
package org.apache.myfaces.custom.fileupload;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.shared_tomahawk.util.WebConfigParamUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.tomahawk.util.ExternalContextUtils;
import org.apache.myfaces.webapp.filter.ExtensionsFilter;
import org.apache.myfaces.webapp.filter.MultipartRequestWrapper;
import org.apache.myfaces.webapp.filter.TomahawkFacesContextFactory;

/**
 * Renderer for the HtmlInputFileUpload component.
 * <p>
 * See also class AbstractHtmlInputFileUpload.
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Input"
 *   type = "org.apache.myfaces.FileUpload"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 782515 $ $Date: 2009-06-07 22:28:42 -0500 (dom, 07 jun 2009) $
 */
public class HtmlFileUploadRenderer
        extends HtmlRenderer
{
    private static final Log log = LogFactory.getLog(HtmlFileUploadRenderer.class);
    
    private static boolean getBooleanInitParameterValue(String initParameter, boolean defaultVal)
    {
        if(initParameter == null || initParameter.trim().length()==0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter.equalsIgnoreCase("true"));
    }  
    
    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
            throws IOException
    {
        super.encodeEnd(facesContext, uiComponent); //check for NP

        //Check the current setup has ExtensionsFilter or TomahawkFacesContextWrapper
        boolean isDisabledTomahawkFacesContextWrapper = getBooleanInitParameterValue(
                WebConfigParamUtils.getStringInitParameter(facesContext.getExternalContext(),
                        TomahawkFacesContextFactory.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER), 
                        TomahawkFacesContextFactory.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER_DEFAULT);

        if (!facesContext.isProjectStage(ProjectStage.Production))
        {
            boolean isPortlet = ExternalContextUtils.getRequestType(facesContext.getExternalContext()).isPortlet();
            //if it is a portlet request and no TomahawkFacesContextWrapper set or
            //is servlet and no ExtensionsFilter/TomahawkFacesContextWrapper set
            //log a warning, because something is not right
            boolean requestInterceptedByFilter = facesContext.getExternalContext().getRequestMap().containsKey(ExtensionsFilter.DOFILTER_CALLED);
            boolean extensionsFilterInitialized = facesContext.getExternalContext().getApplicationMap().containsKey(ExtensionsFilter.EXTENSIONS_FILTER_INITIALIZED); 
            if ( (isPortlet && isDisabledTomahawkFacesContextWrapper) ||
                 (isDisabledTomahawkFacesContextWrapper &&
                  !(requestInterceptedByFilter && extensionsFilterInitialized)))
            {
                if (log.isWarnEnabled())
                {
                    log.warn("t:inputFileUpload requires ExtensionsFilter or TomahawkFacesContextFactory configured to handle multipart file upload, and any of " +
                            "them has been detected. Please read the instructions on http://myfaces.apache.org/tomahawk/extensionsFilter.html " +
                            "for more information about how to setup your environment correctly. Please be sure ExtensionsFilter is the top most " +
                            "filter if you are using multiple jsf libraries.");
                    if (extensionsFilterInitialized && !requestInterceptedByFilter)
                    {
                        log.warn("t:inputFileUpload requires the current request be intercepted by the filter in order to handle forms with multipart encode type. " +
                                "ExtensionsFilter was initialized but the current request was not intercepted. " +
                                "Please add a filter-mapping entry to intercept jsf page requests.");
                    }
                }
            }
        }

        ResponseWriter writer = facesContext.getResponseWriter();
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }
        
        writer.startElement(HTML.INPUT_ELEM, uiComponent);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.FILE_ATTR, null);
        String clientId = uiComponent.getClientId(facesContext);
        if (behaviors != null && !behaviors.isEmpty())
        {
            writer.writeAttribute(HTML.ID_ATTR, clientId,null);
        }
        else
        {
            renderId(facesContext, uiComponent);
        }
        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        UploadedFile value = (UploadedFile)((HtmlInputFileUpload)uiComponent).getValue();
        if (value != null)
        {
            if( value.getName() != null )
            {
                writer.writeAttribute(HTML.VALUE_ATTR, value.getName(), null);
            }
        }
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.INPUT_FILE_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
            HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchange(facesContext, writer, uiComponent, behaviors);
            HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, uiComponent, behaviors);
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.INPUT_FILE_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        }
        
        if (isDisabled(facesContext, uiComponent))
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }

        writer.endElement(HTML.INPUT_ELEM);
    }

    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        if (!UserRoleUtils.isEnabledOnUserRole(uiComponent))
        {
            //if the user is not enabled, the component is
            //disabled, so it should return true.
            return true;
        }
        else
        {
            if (uiComponent instanceof HtmlInputFileUpload)
            {
                return ((HtmlInputFileUpload)uiComponent).isDisabled();
            }
            else
            {
                return RendererUtils.getBooleanAttribute(uiComponent, HTML.DISABLED_ATTR, false);
            }
        }
    }

    private void setSubmittedValueForImplementation(
        FacesContext facesContext, UIComponent uiComponent, FileItem fileItem)
    {
        try
        {
            UploadedFile upFile;
            String implementation =
                ((HtmlInputFileUpload)uiComponent).getStorage();
            if (implementation == null || implementation.length() == 0)
            {
                implementation = "default";
            }
            if (("memory").equals(implementation))
            {
                upFile = new UploadedFileDefaultMemoryImpl(fileItem);
            }
            else if (("default").equals(implementation))
            {
                if (fileItem.isInMemory())
                {
                    upFile = new UploadedFileDefaultMemoryImpl(fileItem);
                }
                else
                {
                    upFile = new UploadedFileDefaultFileImpl(fileItem);                    
                }
            }
            else //"file" case
            {
                upFile = new UploadedFileDefaultFileImpl(fileItem);
            }

            ((HtmlInputFileUpload)uiComponent).setSubmittedValue(upFile);
            ((HtmlInputFileUpload)uiComponent).setValid(true);
        }
        catch (IOException ioe)
        {
            throw new FacesException(
                "Exception while processing file upload for file-input : "
                    + uiComponent.getClientId(facesContext), ioe);
        }
    }

    /**
     * Handle the postback of a form containing a fileUpload component.
     * <p>
     * The browser request will have been in "multi-part-mime" format, where
     * the normal http post is in one part, and the file being uploaded is
     * in another. Hopefully JSF has been configured so that this special
     * request is wrapped in a custom ServletRequest that allows us to
     * fetch that extra data....
     */
    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        super.decode(facesContext, uiComponent); //check for NP

        HtmlRendererUtils.decodeClientBehaviors(facesContext, uiComponent);
        //MultipartWrapper might have been wrapped again by one or more additional
        //Filters. We try to find the MultipartWrapper, but if a filter has wrapped
        //the ServletRequest with a class other than HttpServletRequestWrapper
        //this will fail.
        Object request = facesContext.getExternalContext().getRequest();
        if (!(request instanceof ServletRequest)) {
            ExternalContext externalContext = facesContext.getExternalContext();
            Map fileItems = (Map)externalContext.getRequestMap().
                    get(MultipartRequestWrapper.UPLOADED_FILES_ATTRIBUTE);
            FileItem fileItem = null;
            if (fileItems != null) {
                String paramName = uiComponent.getClientId(facesContext);
                fileItem = (FileItem) fileItems.get(paramName);
            }
            if (fileItem != null)
            {
                setSubmittedValueForImplementation(facesContext, uiComponent, fileItem);
            }
            return;
        }
        if(facesContext.getExternalContext().getRequest() instanceof ServletRequest)
        {
            ServletRequest multipartRequest = (ServletRequest)facesContext.getExternalContext().getRequest();
            while (multipartRequest != null &&
                    !(multipartRequest instanceof MultipartRequestWrapper))
            {
                if (multipartRequest instanceof HttpServletRequestWrapper)
                {
                    multipartRequest = ((HttpServletRequestWrapper)multipartRequest).getRequest();
                }
                else
                {
                    multipartRequest = null;
                }
            }

            if (multipartRequest != null)
            {
                MultipartRequestWrapper mpReq = (MultipartRequestWrapper)multipartRequest;

                String paramName = uiComponent.getClientId(facesContext);
                FileItem fileItem = mpReq.getFileItem(paramName);
                if (fileItem != null)
                {
                    setSubmittedValueForImplementation(facesContext, uiComponent, fileItem);
                }
            }
        }
    }

    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException
    {
        if(submittedValue instanceof UploadedFile)
        {
            UploadedFile file = (UploadedFile) submittedValue;

            if(file.getSize()>0 && file.getName()!=null && file.getName().length()>0)
            {
                return file;
            }
        }

        return null;
    }
}
