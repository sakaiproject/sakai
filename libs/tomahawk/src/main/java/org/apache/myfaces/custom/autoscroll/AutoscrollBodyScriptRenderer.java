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
package org.apache.myfaces.custom.autoscroll;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

/**
 * 
 * @since 1.1.10
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
@JSFRenderer(renderKitId = "HTML_BASIC",
        family = "org.apache.myfaces.custom.autoscroll.AutoscrollBodyScript",
        type = "org.apache.myfaces.custom.autoscroll.AutoscrollBodyScript")
public class AutoscrollBodyScriptRenderer extends HtmlRenderer
{    
    @Override
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException
    {
        //Check npe
        super.encodeEnd(context, component);
        
        //AddResource addResource = AddResourceFactory.getInstance(context);
        
        //Only render it if we are not buffering the request by addResource
        //if (MyfacesConfig.getCurrentInstance(context.getExternalContext()).isAutoScroll())
        //{
            ResponseWriter writer = context.getResponseWriter();
            HtmlRendererUtils.renderAutoScrollFunction(context, writer);
            
            HtmlRendererUtils.renderFormSubmitScript(context);
        //}
    }
}
