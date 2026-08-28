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
package org.apache.myfaces.custom.collapsiblepanel;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.html.ext.HtmlCommandLink;

/**
 * Link used to collapse or expand a t:collapsiblePanel. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:headerLink"
 *   class = "org.apache.myfaces.custom.collapsiblepanel.HtmlHeaderLink"
 *   tagClass = "org.apache.myfaces.custom.collapsiblepanel.HtmlHeaderLinkTag"
 *
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 *
 */
public abstract class AbstractHtmlHeaderLink extends HtmlCommandLink
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlHeaderLink";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HeaderLink";
    
    private static final String LINK_ID = "ToggleCollapsed".intern();
    
    public String getClientId(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");

        String clientId;
        
        //Try to find its nearest parent that extends form HtmlCollapsiblePanel
        //to calculate its id.
        UIComponent collapsiblePanel = findParentCollapsiblePanel(this);
        
        if (collapsiblePanel == null)
        {
            //Calculate its id normally
            clientId = super.getClientId(context);
        }
        else
        {
            //Create its id based on collapsiblePanel id.
            //There only could exists one headerLink per collapsiblePanel.
            String calculatedId = collapsiblePanel.getClientId(context) + LINK_ID;
            
            //Get the original
            clientId = super.getClientId(context);
            
            //just change the final id with the calculated id.
            int lastDoublePointLocation = clientId.lastIndexOf(':');
            clientId = clientId.substring(0,lastDoublePointLocation) +
                calculatedId.substring(lastDoublePointLocation);
        }
        
        return clientId;
    }
    
    protected static UIComponent findParentCollapsiblePanel(UIComponent component)
    {
        UIComponent currentComponent = component;

        // Search for an ancestor that is a instance of HtmlCollapsiblePanel
        while (null != (currentComponent = currentComponent.getParent()))
        {
            if (currentComponent instanceof HtmlCollapsiblePanel)
            {
                break;
            }
        }
        return currentComponent;
    }
    
    /**
     * This property is no longer available since getClientId()
     * is overridden to proper working of collapsiblePanel
     * 
     * @JSFProperty
     *   tagExcluded = "true"
     *   
     * @return
     */
    public Boolean getForceId()
    {
        return Boolean.FALSE;
    }
    
    public void setForceId(Boolean forceId){
        throw new UnsupportedOperationException(); 
    }
    
    /**
     * This property is no longer available since getClientId()
     * is overridden to proper working of collapsiblePanel
     * 
     * @JSFProperty
     *   tagExcluded = "true"
     *   
     * @return
     */
    public Boolean getForceIdIndex()
    {
        return Boolean.TRUE;
    }
    
    public void setForceIdIndex(Boolean forceIdIndex)
    {
        throw new UnsupportedOperationException();
    }

}
