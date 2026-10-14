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
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlMenuRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;


/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.SelectOne"
 *   type = "org.apache.myfaces.Menu"
 *   
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.SelectMany"
 *   type = "org.apache.myfaces.Menu"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 1327287 $ $Date: 2012-04-17 16:21:09 -0500 (Tue, 17 Apr 2012) $
 */
public class HtmlMenuRenderer
        extends HtmlMenuRendererBase
{
    //private static final Log log = LogFactory.getLog(HtmlMenuRenderer.class);
    
    @Override
    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    @Override
    protected boolean isCommonEventsOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }
    
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
            HtmlRendererUtils.renderDisplayValueOnlyForSelects(facesContext, component, true);
        }
        else
        {
            super.encodeEnd(facesContext, component);
        }
    }
    
    /**
     * Overrides HtmlMenuRendererBase to handle valueType attribute on UISelectMany.
     */
    @Override
    public Object getConvertedValue(FacesContext facesContext,
            UIComponent component, Object submittedValue)
            throws ConverterException
    {
        RendererUtils.checkParamValidity(facesContext, component, null);
        
        if (component instanceof UISelectMany) 
        {
            // invoke getConvertedUISelectManyValue() with considerValueType = true
            return RendererUtils.getConvertedUISelectManyValue(facesContext,
                    (UISelectMany) component, submittedValue, true); 
        } 
        else 
        {
            // component is not a UISelectMany --> no change needed
            return super.getConvertedValue(facesContext, component, submittedValue);
        }
    }
    
    /**
     * Overrides HtmlMenuRendererBase to handle valueType attribute on UISelectMany.
     */
    @Override
    protected Converter getConverter(FacesContext facesContext,
            UIComponent component)
    {
        if (component instanceof UISelectMany)
        {
            // invoke findUISelectManyConverterFailsafe() with considerValueType = true
            return HtmlRendererUtils.findUISelectManyConverterFailsafe(facesContext, 
                    (UISelectMany) component, true);
        }
        else
        {
            // component is not a UISelectMany --> no change needed
            return super.getConverter(facesContext, component);
        }
    }

}
