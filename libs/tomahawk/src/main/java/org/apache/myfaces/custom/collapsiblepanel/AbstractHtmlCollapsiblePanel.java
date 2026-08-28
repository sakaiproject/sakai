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

import java.io.IOException;
import java.util.Iterator;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.StyleAware;
import org.apache.myfaces.component.UniversalProperties;
import org.apache.myfaces.component.UserRoleAware;

/**
 * A component which just renders as a single icon (with optional label) when "collapsed", hiding all child components. 
 * 
 * When open, the child components can be seen.
 * 
 * The title attribute defines the label shown for the collapsible panel.
 * 
 * @JSFComponent
 *   name = "t:collapsiblePanel"
 *   class = "org.apache.myfaces.custom.collapsiblepanel.HtmlCollapsiblePanel"  
 *   tagClass = "org.apache.myfaces.custom.collapsiblepanel.HtmlCollapsiblePanelTag"
 * 
 * @JSFJspProperty name = "converter" tagExcluded = "true"
 * @since 1.1.7 
 * @author Kalle Korhonen (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 *
 */
public abstract class AbstractHtmlCollapsiblePanel extends UIInput
     implements StyleAware, UniversalProperties, EventAware,
     UserRoleAware, ClientBehaviorHolder
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlCollapsiblePanel";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Panel";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.CollapsiblePanel";
        
    private static final String HEADER_FACET_NAME = "header";
    
    private static final String CLOSED_CONTENT_FACET_NAME = "closedContent";
        
    private boolean _currentlyCollapsed;
    
    public void setCurrentlyCollapsed(boolean collapsed)
    {
        _currentlyCollapsed = collapsed;
    }

    public boolean isCurrentlyCollapsed()
    {
        return _currentlyCollapsed;
    }
    
    public void setHeader(UIComponent header)
    {
        getFacets().put(HEADER_FACET_NAME, header);
    }

    /**
     * @JSFFacet
     */
    public UIComponent getHeader()
    {
        return (UIComponent) getFacets().get(HEADER_FACET_NAME);
    }
    
    public void setClosedContent(UIComponent closedContent)
    {
        getFacets().put(CLOSED_CONTENT_FACET_NAME, closedContent);
    }

    /**
     * @JSFFacet
     */
    public UIComponent getClosedContent()
    {
        return (UIComponent) getFacets().get(CLOSED_CONTENT_FACET_NAME);
    }
    
    //private static final Log log = LogFactory.getLog(HtmlCollapsiblePanel.class);

    public void processDecodes(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");

        initialiseVars(context);

        if (!isRendered()) return;

        try
        {
            decode(context);
        }
        catch (RuntimeException e)
        {
            context.renderResponse();
            throw e;
        }

        UIComponent headerComponent = getFacet("header");

        if(headerComponent != null)
        {
            for (Iterator it = headerComponent.getChildren().iterator(); it.hasNext(); )
            {
                UIComponent child = (UIComponent)it.next();

                child.processDecodes(context);
            }
        }

        if(isCurrentlyCollapsed())
        {
            UIComponent component = getFacet("closedContent");

            if(component != null)
            {
                component.processDecodes(context);
            }
        }
        else
        {
            for (Iterator it = getChildren().iterator(); it.hasNext(); )
            {
                UIComponent child = (UIComponent)it.next();
                child.processDecodes(context);
            }
        }
        
        removeVars(context);
    }

    public String getClientId(FacesContext context)
    {
        return super.getClientId(context);
    }

    public void processUpdates(FacesContext context)
    {
        initialiseVars(context);

        super.processUpdates(context);

        removeVars(context);
    }

    private void initialiseVars(FacesContext context)
    {
        if(getVar()!=null)
        {
            context.getExternalContext().getRequestMap().put(getVar(),
                            Boolean.valueOf(isCollapsed()));
        }

        if(getTitleVar()!=null)
        {
            context.getExternalContext().getRequestMap().put(getTitleVar(),
                            getTitle());
        }
    }

    private void removeVars(FacesContext context)
    {
        if(getVar()!=null)
        {
            context.getExternalContext().getRequestMap().remove(getVar());
        }

        if(getTitleVar()!=null)
        {
            context.getExternalContext().getRequestMap().remove(getTitleVar());
        }
    }

    public void processValidators(FacesContext context)
    {
        initialiseVars(context);

        super.processValidators(context);

        removeVars(context);
    }

    public void encodeChildren(FacesContext context) throws IOException
    {
        initialiseVars(context);

        super.encodeChildren(context);

        removeVars(context);
    }

    public void updateModel(FacesContext context)
    {
        super.updateModel(context);
    }

    public boolean isCollapsed()
    {
        return isCollapsed(getValue());
    }

    public static boolean isCollapsed(Object collapsedValue)
    {
        Object value = collapsedValue;

        if(value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue();
        }
        else if (value instanceof String)
        {
            return Boolean.valueOf((String) value).booleanValue();
        }

        return true;
    }

    /**
     * The variable which you can use to check for the collapsed 
     * state of the enclosing component. This is especially useful 
     * for custom headers you define in a facet with name 'header'.
     * 
     * @JSFProperty
     */
    public abstract String getVar();

    /**
     * This variable is defined to hold the value of the title 
     * component - you can use it for accessing this value in 
     * custom headers you define in a facet with name 'header'.
     * 
     * @JSFProperty
     */    
    public abstract String getTitleVar();
    
    /**
     * @JSFProperty tagExcluded = "true"
     */
    public MethodBinding getValidator(){
        return super.getValidator();
    }
    
    /**
     * @JSFProperty tagExcluded = "true"
     */
    public MethodBinding getValueChangeListener()
    {
        return super.getValueChangeListener();
    }
    
    /**
     * @JSFProperty tagExcluded = "true"
     */
    public boolean isImmediate()
    {
        return super.isImmediate();
    }
    
    /**
     * @JSFProperty tagExcluded = "true"
     */
    public boolean isRequired()
    {
        return super.isRequired();
    }
    
    /**
     * The CSS class for this element.  Corresponds to the HTML 'class' attribute for the generated indicator span.
     * This attribute is ignored if a custom "header" facet is provided
     * for the collapsible panel 
     * 
     * @return
     */
    @JSFProperty
    public abstract String getIndicatorStyleClass();
    
    /**
     * HTML: CSS styling instructions for the generated indicator. 
     * This attribute is ignored if a custom "header" facet is provided
     * for the collapsible panel 
     * 
     * @return
     */
    @JSFProperty
    public abstract String getIndicatorStyle(); 
    
    /**
     * The CSS class for this element.  Corresponds to the HTML 'class' attribute for the generated title.
     * This attribute is ignored if a custom "header" facet is provided
     * for the collapsible panel 
     * 
     * @return
     */
    @JSFProperty
    public abstract String getTitleStyleClass();
    
    /**
     * HTML: CSS styling instructions for the generated title. 
     * This attribute is ignored if a custom "header" facet is provided
     * for the collapsible panel 
     * 
     * @return
     */
    @JSFProperty
    public abstract String getTitleStyle(); 
}
