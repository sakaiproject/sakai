// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.shared_tomahawk.taglib.core;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import java.util.Locale;
import jakarta.el.MethodExpression;


// Generated from class jakarta.faces.component.UIViewRoot.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class ViewTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public ViewTag()
    {    
    }
    
    public String getComponentType()
    {
        return "jakarta.faces.ViewRoot";
    }

    public String getRendererType()
    {
        return null;
    }

    private MethodExpression _afterPhaseListener;
    
    public void setAfterPhase(MethodExpression afterPhaseListener)
    {
        _afterPhaseListener = afterPhaseListener;
    }
    private MethodExpression _beforePhaseListener;
    
    public void setBeforePhase(MethodExpression beforePhaseListener)
    {
        _beforePhaseListener = beforePhaseListener;
    }
    private ValueExpression _locale;
    
    public void setLocale(ValueExpression locale)
    {
        _locale = locale;
    }
    private ValueExpression _renderKitId;
    
    public void setRenderKitId(ValueExpression renderKitId)
    {
        _renderKitId = renderKitId;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof jakarta.faces.component.UIViewRoot))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no jakarta.faces.component.UIViewRoot");
        }
        
        jakarta.faces.component.UIViewRoot comp = (jakarta.faces.component.UIViewRoot) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_afterPhaseListener != null)
        {
            comp.setAfterPhaseListener(_afterPhaseListener);
        }        
        if (_beforePhaseListener != null)
        {
            comp.setBeforePhaseListener(_beforePhaseListener);
        }        
        if (_locale != null)
        {
            comp.setValueExpression("locale", _locale);
        } 
        if (_renderKitId != null)
        {
            comp.setValueExpression("renderKitId", _renderKitId);
        } 
    }

    public void release()
    {
        super.release();
        _afterPhaseListener = null;
        _beforePhaseListener = null;
        _locale = null;
        _renderKitId = null;
    }
}
