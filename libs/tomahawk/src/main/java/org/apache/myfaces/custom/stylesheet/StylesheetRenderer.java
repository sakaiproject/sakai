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
package org.apache.myfaces.custom.stylesheet;

import java.io.IOException;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.HtmlFileUploadRenderer;
import org.apache.myfaces.custom.stylesheet.TextResourceFilter.ResourceInfo;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.util.WebConfigParamUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.tomahawk.util.ExternalContextUtils;
import org.apache.myfaces.webapp.filter.ExtensionsFilter;
import org.apache.myfaces.webapp.filter.ServeResourcePhaseListener;
import org.apache.myfaces.webapp.filter.TomahawkFacesContextFactory;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Output"
 *   type = "org.apache.myfaces.Stylesheet"
 * 
 * @author mwessendorf (latest modification by $Author: skitching $)
 * @version $Revision: 671131 $ $Date: 2008-06-24 06:19:43 -0500 (mar, 24 jun 2008) $
 */
public class StylesheetRenderer extends HtmlRenderer
{
    private static final Log log = LogFactory.getLog(StylesheetRenderer.class);
    
    private static boolean getBooleanInitParameterValue(String initParameter, boolean defaultVal)
    {
        if(initParameter == null || initParameter.trim().length()==0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter.equalsIgnoreCase("true"));
    }
    
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException, FacesException
    {

        if ((context == null) || (component == null))
        {
            throw new NullPointerException();
        }
        Stylesheet stylesheet = (Stylesheet) component;
        ResponseWriter writer = context.getResponseWriter();

        String path = stylesheet.getPath();

        if (stylesheet.isInline())
        {
            //include as inline css
            
            if (!path.startsWith("/"))
            {
                throw new FacesException("Inline stylesheets require absolute resource path");
            }

            writer.startElement("style", component);
            writer.writeAttribute("type", "text/css", null);
            if (stylesheet.getMedia() != null)
            {
                writer.writeAttribute("media", stylesheet.getMedia(), null);
            }

            Object text;
            if (stylesheet.isFiltered())
            {
                // Load, filter and cache the resource. Then return the cached data.
                ResourceInfo info = TextResourceFilter.getInstance(context).getOrCreateFilteredResource(context, path); 
                text = info.getText();
            }
            else
            {
                // Just load the data (not cached)
                text = RendererUtils.loadResourceFile(context, path);
            }
            if (text != null)
            {
                writer.writeText(text, null);
            }
            writer.endElement("style");
        }
        else
        {
            //refere as link-element
            writer.startElement("link", component);
            writer.writeAttribute("rel", "stylesheet", null);
            writer.writeAttribute("type", "text/css", null);
            if (stylesheet.getMedia() != null)
            {
                writer.writeAttribute("media", stylesheet.getMedia(), null);
            }

            String stylesheetPath;
            if (stylesheet.isFiltered())
            {
                if (!path.startsWith("/"))
                {
                    throw new FacesException("Filtered stylesheets require absolute resource path");
                }

                // Load, filter and cache the resource
                TextResourceFilter.getInstance(context).getOrCreateFilteredResource(context, path);
                
                //Check the current setup has ExtensionsFilter or TomahawkFacesContextWrapper
                //Check the current setup has ExtensionsFilter or TomahawkFacesContextWrapper
                boolean isDisabledTomahawkFacesContextWrapper = getBooleanInitParameterValue(
                        WebConfigParamUtils.getStringInitParameter(context.getExternalContext(),
                                TomahawkFacesContextFactory.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER), 
                                TomahawkFacesContextFactory.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER_DEFAULT);

                if (!context.isProjectStage(ProjectStage.Production))
                {
                    boolean isPortlet = ExternalContextUtils.getRequestType(context.getExternalContext()).isPortlet();
                    //if it is a portlet request and no TomahawkFacesContextWrapper set or
                    //is servlet and no ExtensionsFilter/TomahawkFacesContextWrapper set
                    //log a warning, because something is not right
                    boolean extensionsFilterInitialized = context.getExternalContext().getApplicationMap().containsKey(ExtensionsFilter.EXTENSIONS_FILTER_INITIALIZED); 
                    if ( (isPortlet && isDisabledTomahawkFacesContextWrapper) ||
                         (isDisabledTomahawkFacesContextWrapper && !extensionsFilterInitialized))
                    {
                        if (log.isWarnEnabled())
                        {
                            log.warn("t:stylesheet with filtered=\"true\" requires ExtensionsFilter or TomahawkFacesContextFactory to handle the css resource, and any of " +
                                    "them has been detected. Please read the instructions on http://myfaces.apache.org/tomahawk/extensionsFilter.html " +
                                    "for more information about how to setup your environment correctly. ServeResourcePhaseListener could handle the resource request " +
                                    "too automatically if you use prefix mapping and org.apache.myfaces.RESOURCE_VIRTUAL_PATH match (Default is /faces/myFacesExtensionResource), " +
                                    "so please ignore this warning if you are using it.");
                        }
                    }
                }

                // Compute a URL that goes via the tomahawk ExtensionsFilter and the
                // TextResourceFilterProvider to fetch the resource from the cache.
                //
                // Unfortunately the getResourceUri(context, Class, String, bool) api below is
                // really meant for serving resources out of the Tomahawk jarfile, relative to
                // some class that the resource belongs to. So it only expects to receive
                // relative paths. We are abusing it here to serve resources out of the
                // webapp, specified by an absolute path. So here, the leading slash is
                // stripped off and in the TextResourceFilterProvider a matching hack
                // puts it back on again. A better solution would be to write a custom
                // ResourceHandler class and pass that to the getResourceUri method...
                // TODO: fixme
                String nastyPathHack = path.substring(1);
                stylesheetPath = AddResourceFactory.getInstance(context).getResourceUri(
                        context,
                        TextResourceFilterProvider.class,
                        nastyPathHack,
                        true);
            }
            else
            {
                stylesheetPath = context.getApplication().getViewHandler().getResourceURL(context, path);
            }

            writer.writeURIAttribute("href", stylesheetPath, "path");
            writer.endElement("link");
        }
    }
}
