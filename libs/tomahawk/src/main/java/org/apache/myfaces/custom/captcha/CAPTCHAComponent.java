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
package org.apache.myfaces.custom.captcha;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.captcha.AbstractCAPTCHAComponent.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class CAPTCHAComponent extends org.apache.myfaces.custom.captcha.AbstractCAPTCHAComponent
{

    static public final String COMPONENT_FAMILY =
        "org.apache.myfaces.CAPTCHA";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.CAPTCHA";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.CAPTCHA";


    public CAPTCHAComponent()
    {
        setRendererType("org.apache.myfaces.CAPTCHA");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: captchaSessionKeyName
    public String getCaptchaSessionKeyName()
    {
        return (String) getStateHelper().eval(PropertyKeys.captchaSessionKeyName);
    }
    
    public void setCaptchaSessionKeyName(String captchaSessionKeyName)
    {
        getStateHelper().put(PropertyKeys.captchaSessionKeyName, captchaSessionKeyName ); 
    }    
    // Property: imageWidth
    public String getImageWidth()
    {
        return (String) getStateHelper().eval(PropertyKeys.imageWidth);
    }
    
    public void setImageWidth(String imageWidth)
    {
        getStateHelper().put(PropertyKeys.imageWidth, imageWidth ); 
    }    
    // Property: imageHeight
    public String getImageHeight()
    {
        return (String) getStateHelper().eval(PropertyKeys.imageHeight);
    }
    
    public void setImageHeight(String imageHeight)
    {
        getStateHelper().put(PropertyKeys.imageHeight, imageHeight ); 
    }    

    protected enum PropertyKeys
    {
         captchaSessionKeyName
        , imageWidth
        , imageHeight
    }

 }
