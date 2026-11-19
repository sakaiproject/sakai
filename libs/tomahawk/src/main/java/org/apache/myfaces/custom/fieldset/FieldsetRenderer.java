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
package org.apache.myfaces.custom.fieldset;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.custom.htmlTag.HtmlTagRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

/**
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Output"
 *   type = "org.apache.myfaces.FieldsetRenderer"
 *
 * @author svieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 1393887 $ $Date: 2005-05-11 11:47:12 -0400 (Wed, 11 May 2005) $
 */
public class FieldsetRenderer extends HtmlTagRenderer
{
    public static final String RENDERER_TYPE = "org.apache.myfaces.FieldsetRenderer";

    public boolean getRendersChildren() 
    {
        return true;
    }
    
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {

        Fieldset fieldset = (Fieldset) component;

        if (fieldset.isRendered())
        {
            super.encodeBegin(context, component);
            String legend = fieldset.getLegend();
            if( legend == null || legend.trim().length() == 0 ) // Don't render the legend
                return;
            
            ResponseWriter writer = context.getResponseWriter();

            writer.startElement("legend", fieldset);
            if (fieldset.isEscape())
            {
                writer.writeText(legend , "legend");
            }
            else
            {
                writer.write(legend);
            }
            writer.endElement( "legend" );
        }  
    }
    
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        RendererUtils.renderChildren(context, component);
    }
    
}
