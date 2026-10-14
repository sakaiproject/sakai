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
package org.apache.myfaces.tomahawk.application;

import java.io.IOException;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewHandlerWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

import org.apache.myfaces.custom.autoscroll.AutoscrollBehaviorTagHandler;
import org.apache.myfaces.custom.autoscroll.AutoscrollBodyScript;
import org.apache.myfaces.custom.autoscroll.AutoscrollHiddenField;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

public class ResourceViewHandlerWrapper extends ViewHandlerWrapper
{
    private static final String SET_RESOURCE_CONTAINER_DUMMY_COMPONENT_ID = 
            "oam_autoscroll_setResourceContainer";
    private static final String BODY_SCRIPT_COMPONENT_ID =
            "oam_autoscroll_body_script";
    private static final String HIDDEN_FIELD_COMPONENT_ID =
            "oam_autoscroll_hidden_field";
    
    private ViewHandler _delegate;

    public ResourceViewHandlerWrapper(ViewHandler delegate)
    {
        super();
        _delegate = delegate;
    }

    @Override
    public ViewHandler getWrapped()
    {
        return _delegate;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        UIViewRoot root = super.createView(context, viewId);
        // This listener just ensures that the locations for autoscroll component are
        // set before call markInitialState(). In this way, the state size is reduced when
        // partial state saving is used.
        root.subscribeToViewEvent(PostAddToViewEvent.class, new SystemEventListener() {

            public boolean isListenerForSource(Object o)
            {
                return o instanceof UIViewRoot;
            }

            public void processEvent(SystemEvent se)
            {
                FacesContext context = FacesContext.getCurrentInstance();
                UIViewRoot viewToRender = (UIViewRoot) se.getSource();
                if (MyfacesConfig.getCurrentInstance(context.getExternalContext()).isAutoScroll() ||
                    viewToRender.getAttributes().containsKey(AutoscrollBehaviorTagHandler.AUTOSCROLL_TAG_ON_PAGE))
                {
                    UIOutput test = new UIOutput();
                    test.setId(SET_RESOURCE_CONTAINER_DUMMY_COMPONENT_ID);
                    test.setTransient(true);
                    UIComponent facet = null;
                    facet = viewToRender.getFacet(TomahawkResourceUtils.FORM_LOCATION);
                    if (facet == null)
                    {
                        viewToRender.addComponentResource(context, test, TomahawkResourceUtils.FORM_LOCATION);
                        facet = viewToRender.getFacet(TomahawkResourceUtils.FORM_LOCATION);
                        if (facet != null)
                        {
                            facet.getChildren().remove(test);
                        }
                    }
                    
                    facet = viewToRender.getFacet(TomahawkResourceUtils.BODY_LOCATION);
                    if (facet == null)
                    {
                        viewToRender.addComponentResource(context, test, TomahawkResourceUtils.BODY_LOCATION);
                        facet = viewToRender.getFacet(TomahawkResourceUtils.BODY_LOCATION);
                        if (facet != null)
                        {
                            facet.getChildren().remove(test);
                        }
                    }

                    facet = viewToRender.getFacet(TomahawkResourceUtils.HEAD_LOCATION);
                    if (facet == null)
                    {
                        viewToRender.addComponentResource(context, test, TomahawkResourceUtils.HEAD_LOCATION);
                        facet = viewToRender.getFacet(TomahawkResourceUtils.HEAD_LOCATION);
                        if (facet != null)
                        {
                            facet.getChildren().remove(test);
                        }
                    }
                }
            }
        });
        return root;
    }

    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender)
            throws IOException, FacesException
    {
        if (MyfacesConfig.getCurrentInstance(context.getExternalContext()).isAutoScroll() ||
            viewToRender.getAttributes().containsKey(AutoscrollBehaviorTagHandler.AUTOSCROLL_TAG_ON_PAGE))
        {
            AddResource addResource = AddResourceFactory.getInstance(context);

            if (!addResource.requiresBuffer())
            {
                //If the response is buffered, addResource instance takes
                //the responsability of render this script.
                AutoscrollBodyScript autoscrollBodyScript = (AutoscrollBodyScript) 
                    context.getApplication().createComponent(context, 
                        AutoscrollBodyScript.COMPONENT_TYPE,
                        AutoscrollBodyScript.DEFAULT_RENDERER_TYPE);
                autoscrollBodyScript.setId(BODY_SCRIPT_COMPONENT_ID);
                autoscrollBodyScript.setTransient(true);
                autoscrollBodyScript.getAttributes().put(
                        JSFAttr.TARGET_ATTR, 
                        TomahawkResourceUtils.BODY_LOCATION);
                viewToRender.addComponentResource(context, autoscrollBodyScript);
            }
            
            AutoscrollHiddenField autoscrollHiddenField = (AutoscrollHiddenField) context.getApplication().
                createComponent(context, 
                        AutoscrollHiddenField.COMPONENT_TYPE,
                        AutoscrollHiddenField.DEFAULT_RENDERER_TYPE);
            autoscrollHiddenField.setId(HIDDEN_FIELD_COMPONENT_ID);
            autoscrollHiddenField.setTransient(true);
            autoscrollHiddenField.getAttributes().put(JSFAttr.TARGET_ATTR, TomahawkResourceUtils.FORM_LOCATION);
            viewToRender.addComponentResource(context, autoscrollHiddenField);
        }
        
        //Reset the added resource 
        TomahawkResourceUtils.resetAddedResources(context);
        
        _publishPreRenderViewAddResourceEvent(context, viewToRender);
        super.renderView(context, viewToRender);
    }
    
    private void _publishPreRenderViewAddResourceEvent(FacesContext context, UIComponent component)
    {
        context.getApplication().publishEvent(context,  PreRenderViewAddResourceEvent.class, UIComponent.class, component);
        
        //Scan children
        if (component.getChildCount() > 0)
        {
            for (UIComponent child : component.getChildren())
            {
                _publishPreRenderViewAddResourceEvent(context, child);
            }
        }

        //Scan facets
        if (component.getFacetCount() > 0)
        {
            for (Map.Entry<String, UIComponent> entry : component.getFacets().entrySet())
            {
                _publishPreRenderViewAddResourceEvent(context, entry.getValue());
            }
        }
    }
}
