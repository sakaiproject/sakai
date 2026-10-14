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
package org.apache.myfaces.custom.tree.renderkit.html;

import java.io.IOException;
import java.util.Set;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import org.apache.myfaces.custom.tree.HtmlTreeCheckbox;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlCheckboxRendererBase;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "org.apache.myfaces.HtmlTreeCheckbox"
 *   type = "org.apache.myfaces.HtmlTreeCheckbox"
 *   
 * @author <a href="mailto:dlestrat@apache.org">David Le Strat </a>
 */
public class HtmlTreeCheckboxRenderer extends HtmlCheckboxRendererBase
{

    /**
     * @see jakarta.faces.render.Renderer#encodeEnd(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {
        HtmlTreeCheckbox checkbox = (HtmlTreeCheckbox) component;

        String forAttr = checkbox.getFor();
        if (forAttr == null)
        {
            throw new IllegalStateException("Mandatory attribute 'for'");
        }

        UIComponent uiComponent = checkbox.findComponent(forAttr);
        if (uiComponent == null)
        {
            throw new IllegalStateException("Could not find component '" + forAttr
                                            + "' (calling findComponent on component '" + checkbox.getClientId(context) + "')");
        }
        if (!(uiComponent instanceof UISelectMany))
        {
            throw new IllegalStateException("UISelectMany expected");
        }

        UISelectMany uiSelectMany = (UISelectMany) uiComponent;

        Converter converter;
        try
        {
            converter = RendererUtils.findUISelectManyConverter(context, uiSelectMany);
        }
        catch (FacesException e)
        {
            converter = null;
        }

        Set lookupSet = RendererUtils.getSelectedValuesAsSet(context, component, converter, uiSelectMany);

        Object itemValue = checkbox.getItemValue();
        String itemStrValue = null;
        if (converter == null)
        {
            if (null != itemValue)
            {
                itemStrValue = itemValue.toString();
            }
        }
        else
        {
            itemStrValue = converter.getAsString(context, uiSelectMany, itemValue);
        }

        renderCheckbox(context,
                       uiSelectMany,
                       itemStrValue,
                       checkbox.getItemLabel(),
                       isDisabled(context,uiSelectMany),
                       lookupSet.contains(itemStrValue),
                       true);
    }

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlCheckboxRendererBase#isDisabled(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        return super.isDisabled(facesContext, uiComponent);
    }

    /**
     * @see jakarta.faces.render.Renderer#decode(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        if (uiComponent instanceof HtmlTreeCheckbox)
        {
            //nothing to decode
        }
        else
        {
            super.decode(facesContext, uiComponent);
        }
    }
}
