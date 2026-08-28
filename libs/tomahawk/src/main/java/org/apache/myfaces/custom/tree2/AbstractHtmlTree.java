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
package org.apache.myfaces.custom.tree2;

import java.util.Map;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.component.LibraryLocationAware;
import org.apache.myfaces.component.LocationAware;

/**
 * Represents "tree data" in an HTML format.  Also provides a mechanism for maintaining expand/collapse
 * state of the nodes in the tree.
 *
 * A component that provides an HTML-based tree from data supplied by a 
 * backing bean. The tree is highly customizable and allows for 
 * fine-grained control over the appearance of each of the nodes 
 * depending on their type. 
 * 
 * Almost any type of JSF component (text, image, checkbox, etc.) can 
 * be rendered inside the nodes and there is an option for client-side 
 * or server-side toggling of the expand/collapse state. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @since 1.1.7
 * @author Sean Schofield
 */
@JSFComponent(
    name = "t:tree2",
    clazz = "org.apache.myfaces.custom.tree2.HtmlTree",
    tagClass = "org.apache.myfaces.custom.tree2.TreeTag",
    tagHandler = "org.apache.myfaces.custom.tree2.HtmlTreeTagHandler")
public abstract class AbstractHtmlTree extends UITreeData
    implements LocationAware, LibraryLocationAware
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTree2";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTree2";

    private UICommand _expandControl = null;

    /**
     * Perform client-side toggling of expand/collapse state via javascript (default is true.)
     * 
     * @return  the new clientSideToggle value
     */
    @JSFProperty(defaultValue = "true")
    public boolean isClientSideToggle()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.clientSideToggle, true);
    }
    
    /**
     * Sets 
     * 
     * @param clientSideToggle  the new clientSideToggle value
     */
    public void setClientSideToggle(boolean clientSideToggle)
    {
        getStateHelper().put(PropertyKeys.clientSideToggle, clientSideToggle ); 
    }  
        
    /**
     * @see org.apache.myfaces.custom.tree2.UITreeData#processNodes(jakarta.faces.context.FacesContext, int, org.apache.myfaces.custom.tree2.TreeWalker)
     */
    protected void processNodes(FacesContext context, int processAction, TreeWalker walker)
    {
        if (isClientSideToggle()) {
            walker.setCheckState(false);
        }
        super.processNodes(context, processAction, walker);
    }
    
    //public abstract String getLocalVarNodeToggler();

    // see superclass for documentation
    public void setNodeId(String nodeId)
    {
        super.setNodeId(nodeId);

        String varNodeToggler = (String) getStateHelper().get(PropertyKeys.varNodeToggler);
        if (varNodeToggler != null)
        {
            Map requestMap = getFacesContext().getExternalContext().getRequestMap();
            requestMap.put(varNodeToggler, this);
        }
    }

    /**
     * Gets the expand/collapse control that can be used to handle expand/collapse nodes.  This is only used in server-side
     * mode.  It allows the nagivation controls (if any) to be clickable as well as any commandLinks the user has set up in
     * their JSP.
     * 
     * @return UICommand
     */
    public UICommand getExpandControl()
    {
        if (_expandControl == null){
            _expandControl = new HtmlCommandLink();
            _expandControl.setParent(this);
        }
        return _expandControl;
    }
    
    /**
     * Gets 
     *
     * @return  the new varNodeToggler value
     */
    @JSFProperty(literalOnly=true)
    public String getVarNodeToggler()
    {
        return (String) getStateHelper().get(PropertyKeys.varNodeToggler);
    }
    
    public void setVarNodeToggler(String varNodeToggler)
    {
        getStateHelper().put(PropertyKeys.varNodeToggler, varNodeToggler );

        // create a method binding for the expand control
        String bindingString = "#{" + varNodeToggler + ".toggleExpanded}";
        MethodBinding actionBinding = FacesContext.getCurrentInstance().getApplication().createMethodBinding(bindingString, null);
        getExpandControl().setAction(actionBinding);
    }
        
    /**
     * Show the "plus" and "minus" navigation icons (default is true.) 
     * Value is ignored if clientSideToggle is true.
     * 
     */
    @JSFProperty(defaultValue="true")
    public abstract boolean isShowNav();

    /**
     * Show the connecting lines (default is true.)
     * 
     */
    @JSFProperty(defaultValue="true")
    public abstract boolean isShowLines();

    /**
     * Include the root node when rendering the tree (default is true.)
     * 
     */
    @JSFProperty(defaultValue="true")
    public abstract boolean isShowRootNode();

    /**
     * Preserve changes in client-side toggle information between 
     * requests (default is true.)
     * 
     */
    @JSFProperty(defaultValue="true")
    public abstract boolean isPreserveToggle();

    protected enum PropertyKeys
    {
         clientSideToggle
        , varNodeToggler
    }
}
