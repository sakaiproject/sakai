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
package org.apache.myfaces.custom.toggle;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlGroupRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.TogglePanel"
 * 
 */
@ResourceDependency(library="oam.custom.toggle", name="MyFacesToggleLink.js")
public class TogglePanelRenderer extends HtmlGroupRendererBase {

    private static Log log = LogFactory.getLog(TogglePanelRenderer.class);

    private static final String LAYOUT_BLOCK_VALUE = "block";

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);
        
        HtmlRendererUtils.decodeClientBehaviors(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        RendererUtils.checkParamValidity(context, component, TogglePanel.class);
        //addToggleLinkJavascript(context);
        
        TogglePanel togglePanel = (TogglePanel) component;
        // render the hidden input field
        ResponseWriter writer = context.getResponseWriter();
        
        Map<String, List<ClientBehavior>> behaviors = null;
        behaviors = togglePanel.getClientBehaviors();
        if (!behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(context, writer);
        }
        
        boolean toggleMode = togglePanel.isToggled();
        toggleVisibility(togglePanel.getChildren(), toggleMode);

        String hiddenFieldId = getHiddenFieldId(context, togglePanel);

        writer.startElement(HTML.INPUT_ELEM, component);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        writer.writeAttribute(HTML.ID_ATTR, hiddenFieldId, null);
        writer.writeAttribute(HTML.NAME_ATTR, hiddenFieldId, null);

        writer.writeAttribute(HTML.VALUE_ATTR, toggleMode ? "1" : "", null);

        writer.endElement(HTML.INPUT_ELEM);

        //super.encodeEnd(context, togglePanel);
        boolean span = false;

        // will be SPAN or DIV, depending on the layout attribute value
        String layoutElement = HTML.SPAN_ELEM;

        // if layout is 'block', render DIV instead SPAN
        String layout = togglePanel.getLayout();
        if (layout != null && layout.equals(LAYOUT_BLOCK_VALUE))
        {
            layoutElement = HTML.DIV_ELEM;
        }

        if (behaviors != null && !behaviors.isEmpty())
        {
            //Render element and id to make javascript work
            span = true;
            writer.startElement(layoutElement, component);
            writer.writeAttribute(HTML.ID_ATTR, component.getClientId(context),null);
            HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.UNIVERSAL_ATTRIBUTES);
            HtmlRendererUtils.renderBehaviorizedEventHandlers(context, writer, component, behaviors);
        }
        else
        {
            //No behaviors, do it as usual
            if(component.getId()!=null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
            {
                span = true;

                writer.startElement(layoutElement, component);

                HtmlRendererUtils.writeIdIfNecessary(writer, component, context);

                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }
            else
            {
                span=HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(writer,
                                                                                 component,
                                                                                 layoutElement,
                                                                                 HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }
        }

        RendererUtils.renderChildren(context, component);
        if (span)
        {
            writer.endElement(layoutElement);
        }
    }

    private void toggleVisibility(List children, boolean toggleMode) {
        for(Iterator it = children.iterator(); it.hasNext(); ) {
            UIComponent component = (UIComponent) it.next();
            setComponentVisibility( component, toggleMode );
        }
    }

    // checks if this component has getStyle/setStyle methods
    public static boolean hasStyleAttribute(UIComponent component) {
        Method[] methods = component.getClass().getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("getStyle")) {
                return true;
            }
        }
        return false;
    }

    // hides component by appending 'display:none' to the 'style' attribute
    public static void setComponentVisibility(UIComponent component, boolean toggleMode) {
        FacesContext context = FacesContext.getCurrentInstance();

        if (!hasStyleAttribute(component)) {
            log.info("style attribute expected, not found for component " + component.getClientId(context));
            return;
        }

        try {
            Class c = component.getClass();
            Method getStyle = c.getMethod("getStyle", new Class[] {});
            Method setStyle = c.getMethod("setStyle", new Class[] { String.class });

            String style = (String) getStyle.invoke(component, new Object[] {});
            
            boolean display = toggleMode != isHiddenWhenToggled(component);
            if( display ){
                if (style == null || style.length() == 0) {
                    return;
                } else {
                    int index = style.indexOf(";display:none;");
                    if (index == -1)
                        return;

                    if (index == 0) {
                        style = null;
                    } else {
                        style = style.substring(0, index);
                    }
                }
            }else{ // hide
                if (style == null) {
                    style = ";display:none;";
                } else if (style.indexOf("display:none;") == -1) {
                    style = style.concat(";display:none;");
                }
            }

            setStyle.invoke(component, new Object[] { style });
        } catch (Throwable e) {
            log.error("unable to set style attribute on component " + component.getClientId(context));
        }
    }

    public static boolean isHiddenWhenToggled(UIComponent component){
        return component instanceof ToggleLink || component instanceof ToggleGroup;
    }

    private String getHiddenFieldId(FacesContext context, TogglePanel togglePanel){
        return togglePanel.getClientId(context) + "_hidden";
    }

    static public String getToggleJavascriptFunctionName(FacesContext context, TogglePanel togglePanel) {
        return "MyFacesToggleLinkUtils.toggle";
    }
    
    /*
    public void addToggleLinkJavascript(FacesContext context)throws IOException {
        AddResource addResource = AddResourceFactory.getInstance(context);
        
        addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, AbstractTogglePanel.class, "MyFacesToggleLink.js");
    }*/
}
