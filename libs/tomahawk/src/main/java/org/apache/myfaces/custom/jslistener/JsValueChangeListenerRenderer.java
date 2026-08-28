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
package org.apache.myfaces.custom.jslistener;

import java.io.IOException;

import jakarta.faces.application.Application;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Output"
 *   type = "org.apache.myfaces.JsValueChangeListener"
 * 
 * @author Martin Marinschek (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (jue, 03 jul 2008) $
 */
@ResourceDependency(library="oam.custom.jslistener",name="JSListener.js")
public class JsValueChangeListenerRenderer
        extends HtmlRenderer
{
    private static Log log = LogFactory.getLog(JsValueChangeListenerRenderer.class);

    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, JsValueChangeListener.class);

        UIComponent parent = component.getParent();

        JsValueChangeListener jsValueChangeListener = (JsValueChangeListener) component;


        String aFor = jsValueChangeListener.getFor();
        String expressionValue = jsValueChangeListener.getExpressionValue();
        String property = jsValueChangeListener.getProperty();

        //AddResourceFactory.getInstance(facesContext).addJavaScriptAtPosition(
        //        facesContext, AddResource.HEADER_BEGIN, JsValueChangeListenerRenderer.class,
        //        "JSListener.js");

        if(aFor!=null)
        {
            UIComponent forComponent = component.findComponent(aFor);

            String forComponentId = null;

            if (forComponent == null)
            {
                if (log.isInfoEnabled())
                {
                    log.info("Unable to find component '" + aFor + "' (calling findComponent on component '" + component.getClientId(getFacesContext()) + "') - will try to render component id based on the parent-id (on same level)");
                }
                if (aFor.length() > 0 && aFor.charAt(0) == UINamingContainer.SEPARATOR_CHAR)
                {
                    //absolute id path
                    forComponentId = aFor.substring(1);
                }
                else
                {
                    //relative id path, we assume a component on the same level as the label component
                    String labelClientId = component.getClientId(getFacesContext());
                    int colon = labelClientId.lastIndexOf(UINamingContainer.SEPARATOR_CHAR);
                    if (colon == -1)
                    {
                        forComponentId = aFor;
                    }
                    else
                    {
                        forComponentId = labelClientId.substring(0, colon + 1) + aFor;
                    }
                }
            }
            else
            {
                forComponentId = forComponent.getClientId(getFacesContext());
            }

            expressionValue = expressionValue.replaceAll("\\'","\\\\'");
            expressionValue = expressionValue.replaceAll("\"","\\\"");


            String methodCall = "orgApacheMyfacesJsListenerSetExpressionProperty('"+
                    parent.getClientId(getFacesContext())+"','"+
                    forComponentId+"',"+
                    (property==null?"null":"'"+property+"'")+
                    ",'"+expressionValue+"');";


            callMethod(facesContext, jsValueChangeListener, "onchange",methodCall);

            if (jsValueChangeListener.getBodyTagEvent() != null)
            {
                callMethod(facesContext, jsValueChangeListener, jsValueChangeListener.getBodyTagEvent(), methodCall);
            }

        }
    }

    private void callMethod(FacesContext context, JsValueChangeListener jsValueChangeListener, String propName, String value)
    {
        UIComponent parent = jsValueChangeListener.getParent();

        Object oldValue = parent.getAttributes().get(propName);
        
        if(oldValue != null)
        {
            String oldValueStr = oldValue.toString().trim();

            // render the jsValueChangeListener script only for each parent component 
            if(oldValueStr.indexOf(parent.getClientId(FacesContext.getCurrentInstance())) < 0
                    && oldValueStr.length() > 0)
            {
                oldValueStr = "";
            }

            if(oldValueStr.length()>0 && !oldValueStr.endsWith(";"))
                oldValueStr +=";";

            value = oldValueStr + value;

        }

        if (!propName.equals("onchange") && value != null)
        {
            //TODO: this should be refactored to register a javascript-event-wrapper the java-script way
            //in the current version of AddResource, this would not work anymore
            /*AddResourceFactory.getInstance(context).
                    addJavaScriptToBodyTag(context,jsValueChangeListener.getBodyTagEvent(), value);*/
        }
        else if(value != null)
        {
            parent.getAttributes().put(propName, value);
        }
        else
        {
            try
            {
                parent.getAttributes().remove(propName);
            }
            catch(Exception ex)
            {
                log.error("the value could not be removed : ",ex);
            }
        }
    }


    protected Application getApplication()
    {
        return getFacesContext().getApplication();
    }

    protected FacesContext getFacesContext()
    {
        return FacesContext.getCurrentInstance();
    }

}
