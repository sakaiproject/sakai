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
package org.apache.myfaces.renderkit.html.ext;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlSecretRendererBase;


/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.Input"
 *   type = "org.apache.myfaces.Secret"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @author Bruno Aranda
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (Sat, 24 May 2008) $
 */
public class HtmlSecretRenderer
        extends HtmlSecretRendererBase
{
    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        if (!UserRoleUtils.isEnabledOnUserRole(uiComponent))
        {
            return true;
        }
        else
        {
            return super.isDisabled(facesContext, uiComponent);
        }
    }

    public void encodeEnd(FacesContext facesContext, UIComponent component)
    throws IOException
    {
        if(HtmlRendererUtils.isDisplayValueOnly(component))
        {
            renderInputValueOnly(facesContext,
                                 (UIInput) component);
        }
        else
        {
            super.encodeEnd(facesContext, component);
        }
    }

    protected void renderInputValueOnly(FacesContext facesContext, UIInput uiInput)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SPAN_ELEM, uiInput);

        HtmlRendererUtils.writeIdIfNecessary(writer, uiInput, facesContext);

        HtmlRendererUtils.renderDisplayValueOnlyAttributes(uiInput, writer);

        // renders five asterisks instead of the component value
        writer.writeText("*****", JSFAttr.VALUE_ATTR);

        writer.endElement(HTML.SPAN_ELEM);
    }
}
