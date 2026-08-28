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
package org.apache.myfaces.custom.inputTextHelp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;

import org.apache.myfaces.renderkit.html.ext.HtmlTextRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.Input"
 *   type = "org.apache.myfaces.TextHelp"
 * 
 * @author Thomas Obereder
 * @version Date: 09.06.2005, 22:50:48
 */
@ResourceDependency(library="oam.custom.inputTextHelp", name="inputTextHelp.js")
public class HtmlTextHelpRenderer extends HtmlTextRenderer
{
    private static final String JAVASCRIPT_ENCODED = "org.apache.myfaces.inputTextHelp.JAVASCRIPT_ENCODED";

    protected void renderNormal(FacesContext facesContext, UIComponent component) throws IOException
    {
        if(component instanceof HtmlInputTextHelp)
        {
            HtmlInputTextHelp helpTextComp = (HtmlInputTextHelp) component;
            //addJavaScriptResources(facesContext);
            renderInputTextHelp(facesContext, helpTextComp);
        }
        else
        {
            super.renderNormal(facesContext, component);
        }
    }

    public static boolean isSelectText(UIComponent component)
    {
        if(component instanceof HtmlInputTextHelp)
        {
            HtmlInputTextHelp helpTextComp = (HtmlInputTextHelp) component;
            return helpTextComp.isSelectText();
        }
        return false;
    }

    public static String getHelpText(UIComponent component)
    {
        if(component instanceof HtmlInputTextHelp)
        {
            HtmlInputTextHelp helpTextComp = (HtmlInputTextHelp) component;
            if(helpTextComp.getHelpText() != null)
                return helpTextComp.getHelpText();
        }
        return null;
    }

    public void renderInputTextHelp(FacesContext facesContext, UIInput input)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        Map<String, List<ClientBehavior>> behaviors = null;
        if (input instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) input).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }
        
        writer.startElement(HTML.INPUT_ELEM, input);

        writer.writeAttribute(HTML.ID_ATTR, input.getClientId(facesContext), null);
        
        String name = (String) input.getAttributes().get("name");
        if (name == null)
        {
            name = input.getClientId(facesContext);
        }
        writer.writeAttribute(HTML.NAME_ATTR, name, null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_TEXT, null);

        renderHelpTextAttributes(input, writer, facesContext);

        String value = RendererUtils.getStringValue(facesContext, input);
        value = (value==null || value.length()==0) ? getHelpText(input) : value;

        if (value != null)
        {
            writer.writeAttribute(HTML.VALUE_ATTR, value, JSFAttr.VALUE_ATTR);
        }

        writer.endElement(HTML.INPUT_ELEM);
    }

    private void renderHelpTextAttributes(UIComponent component,
                                          ResponseWriter writer,
                                          FacesContext facesContext)
            throws IOException
    {
        Map<String, List<ClientBehavior>> behaviors = null;
        if (component instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder)component).getClientBehaviors();
        }
        if (behaviors != null && !behaviors.isEmpty())
        {
            String targetClientId = (String) component.getAttributes().get("targetClientId");
            if (targetClientId == null)
            {
                targetClientId = component.getClientId(facesContext);
            }
            
            HtmlRendererUtils.renderBehaviorizedEventHandlersWithoutOnclick(facesContext, writer, component, behaviors);
            HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, component, targetClientId, behaviors);
            HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, component, targetClientId,
                    ClientBehaviorEvents.BLUR, behaviors, HTML.ONBLUR_ATTR);
            HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer,  HTML.ONSELECT_ATTR, component, targetClientId,
                    ClientBehaviorEvents.SELECT, behaviors, HTML.ONSELECT_ATTR);
            HtmlRendererUtils.renderHTMLAttributes(writer, component,
                    HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);

            if(!(component instanceof HtmlInputTextHelp) || getHelpText(component) == null || ("").equals(getHelpText(component).trim()))
            {
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, component, targetClientId,
                        ClientBehaviorEvents.FOCUS, behaviors, HTML.ONFOCUS_ATTR);
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, component, targetClientId,
                        ClientBehaviorEvents.CLICK, behaviors, HTML.ONCLICK_ATTR);
            }
            else
            {
                String id = component.getClientId(facesContext);
                HtmlInputTextHelp textHelp = (HtmlInputTextHelp)component;

                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, component, targetClientId,
                        ClientBehaviorEvents.FOCUS, null, behaviors, HTML.ONFOCUS_ATTR, null,
                        buildJavascriptFunction(component, id, textHelp.getOnfocus()));
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, component, targetClientId,
                        ClientBehaviorEvents.CLICK, null, behaviors, HTML.ONCLICK_ATTR, null,
                        buildJavascriptFunction(component, id, textHelp.getOnclick()));
            }
        }
        else
        {
            if(!(component instanceof HtmlInputTextHelp) || getHelpText(component) == null || ("").equals(getHelpText(component).trim()))
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
            }
            else
            {
                String id = component.getClientId(facesContext);
                HtmlInputTextHelp textHelp = (HtmlInputTextHelp)component;

                HtmlRendererUtils.renderHTMLAttributes(writer, component,
                                                   HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_ONFOCUS_AND_ONCLICK);
                writer.writeAttribute(HTML.ONFOCUS_ATTR,
                                  buildJavascriptFunction(component, id, textHelp.getOnfocus()), null);
                writer.writeAttribute(HTML.ONCLICK_ATTR,
                                  buildJavascriptFunction(component, id, textHelp.getOnclick()), null);
            }
        }

        if (isDisabled(facesContext, component))
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }
    }
    
    public static void renderBehaviorizedEventHandlersWithoutOnclick(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.DBLCLICK, clientBehaviors, HTML.ONDBLCLICK_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEDOWN_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEDOWN, clientBehaviors, HTML.ONMOUSEDOWN_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEUP, clientBehaviors, HTML.ONMOUSEUP_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOVER_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEOVER, clientBehaviors, HTML.ONMOUSEOVER_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEMOVE_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEMOVE, clientBehaviors, HTML.ONMOUSEMOVE_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEOUT, clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYPRESS, clientBehaviors, HTML.ONKEYPRESS_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYDOWN, clientBehaviors, HTML.ONKEYDOWN_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYUP, clientBehaviors, HTML.ONKEYUP_ATTR);
    }    
    
    private String buildJavascriptFunction(UIComponent component, String id, String componentScript) 
    {
        StringBuffer jsFunction = new StringBuffer();
        if(isSelectText(component))
        {
            jsFunction.append(HtmlInputTextHelp.JS_FUNCTION_SELECT_TEXT);
            jsFunction.append("('");
            jsFunction.append(    getHelpText(component)).append("', '");
            jsFunction.append(    id);
            jsFunction.append("')");
        }
        else
        {
            jsFunction.append(HtmlInputTextHelp.JS_FUNCTION_RESET_HELP );
            jsFunction.append("('");
            jsFunction.append(    getHelpText(component)).append("', '");
            jsFunction.append(    id);
            jsFunction.append("')");
        }
        
        if(componentScript != null && !("").equals(componentScript.trim())) {
            jsFunction.append(";");
            jsFunction.append(componentScript);
        }
        
        return jsFunction.toString();
    }

    public void decode(FacesContext facesContext, UIComponent component)
    {
        super.decode(facesContext, component);
    }

    public Object getConvertedValue(FacesContext facesContext, UIComponent component, Object submittedValue) throws ConverterException
    {
        if(submittedValue!=null && component instanceof HtmlInputTextHelp &&
           ((HtmlInputTextHelp) component).getHelpText()!=null &&
           submittedValue.equals(((HtmlInputTextHelp) component).getHelpText()))
        {
            submittedValue = "";
        }

        return super.getConvertedValue(facesContext, component, submittedValue);
    }

    /*public static void addJavaScriptResources(FacesContext facesContext)
    {
        // check to see if javascript has already been written (which could happen if more than one calendar on the same page)
        if (facesContext.getExternalContext().getRequestMap().containsKey(JAVASCRIPT_ENCODED))
        {
            return;
        }

        AddResourceFactory.getInstance(facesContext).addJavaScriptAtPosition(
                facesContext, AddResource.HEADER_BEGIN, HtmlTextHelpRenderer.class, "inputTextHelp.js");

        facesContext.getExternalContext().getRequestMap().put(JAVASCRIPT_ENCODED, Boolean.TRUE);
    }*/
}
