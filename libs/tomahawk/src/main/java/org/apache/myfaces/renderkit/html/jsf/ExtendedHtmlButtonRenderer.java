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
package org.apache.myfaces.renderkit.html.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.renderkit.html.util.DummyFormUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlButtonRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;


/**
 * Add dummyForm functionality 
 * 
 * @author Mario Ivankovits (latest modification by $Author: lu4242 $)
 * @version $Revision: 709236 $ $Date: 2008-10-30 15:22:57 -0500 (Thu, 30 Oct 2008) $
 */
public class ExtendedHtmlButtonRenderer
    extends HtmlButtonRendererBase
{
    protected void addHiddenCommandParameter(FacesContext facesContext, UIComponent nestingForm, String hiddenFieldName)
    {
        if (nestingForm != null)
        {
            super.addHiddenCommandParameter(facesContext, nestingForm, hiddenFieldName);
        }
        else
        {
            DummyFormUtils.addDummyFormParameter(facesContext, hiddenFieldName);
        }
    }
    
    protected FormInfo findNestingForm(UIComponent uiComponent, FacesContext facesContext)
    {
        return DummyFormUtils.findNestingForm(uiComponent, facesContext);
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
}
