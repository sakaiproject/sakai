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
package org.apache.myfaces.renderkit.html.ext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.model.SelectItem;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.custom.radio.HtmlRadio;
import org.apache.myfaces.shared_tomahawk.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRadioRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;


/**
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "org.apache.myfaces.Radio"
 *   type = "org.apache.myfaces.Radio"
 *    
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.SelectOne"
 *   type = "org.apache.myfaces.Radio"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @author Thomas Spiegl
 * @version $Revision: 685654 $ $Date: 2008-08-13 14:57:50 -0500 (mié, 13 ago 2008) $
 */
public class HtmlRadioRenderer
        extends HtmlRadioRendererBase
{
    //private static final Log log = LogFactory.getLog(HtmlRadioRenderer.class);

    private static final String LAYOUT_SPREAD = "spread";
    
    private static final String[] LABEL_STYLES = { "labelStyle", "labelStyleClass" };

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (component == null) throw new NullPointerException("component");

        if (component instanceof HtmlRadio)
        {
            renderRadio(context, (HtmlRadio)component);
        }
        else if (HtmlRendererUtils.isDisplayValueOnly(component))
        {
            HtmlRendererUtils.renderDisplayValueOnlyForSelects(context, component);
        }
        else if (component instanceof UISelectOne)
        {
            String layout = getLayout(component);
            if (layout != null && layout.equals(LAYOUT_SPREAD))
            {
                return; //radio inputs are rendered by spread radio components
            }
            else
            {
                super.encodeEnd(context, component);
            }
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + component.getClass().getName());
        }
    }

    protected void renderRadio(FacesContext facesContext, HtmlRadio radio) throws IOException
    {
        String forAttr = radio.getFor();
        if (forAttr == null)
        {
            throw new IllegalStateException("mandatory attribute 'for'");
        }
        int index = radio.getIndex();
        if (index < 0)
        {
            throw new IllegalStateException("positive index must be given");
        }

        UIComponent uiComponent = radio.findComponent(forAttr);
        if (uiComponent == null)
        {
            throw new IllegalStateException("Could not find component '" + forAttr + "' (calling findComponent on component '" + radio.getClientId(facesContext) + "')");
        }
        if (!(uiComponent instanceof UISelectOne))
        {
            throw new IllegalStateException("UISelectOne expected");
        }

        UISelectOne uiSelectOne = (UISelectOne)uiComponent;
        Converter converter;
        List selectItemList = RendererUtils.getSelectItemList(uiSelectOne);
        if (index >= selectItemList.size())
        {
            throw new IndexOutOfBoundsException("index " + index + " >= " + selectItemList.size());
        }

        try
        {
            converter = RendererUtils.findUIOutputConverter(facesContext, uiSelectOne);
        }
        catch (FacesException e)
        {
            converter = null;
        }

        Object currentValue = RendererUtils.getObjectValue(uiSelectOne);
        currentValue
            = RendererUtils.getConvertedStringValue(facesContext, uiSelectOne,
                                                    converter, currentValue);
        SelectItem selectItem = (SelectItem)selectItemList.get(index);
        String itemStrValue
            = RendererUtils.getConvertedStringValue(facesContext, uiSelectOne,
                                                    converter,
                                                    selectItem.getValue());

        ResponseWriter writer = facesContext.getResponseWriter();

        //writer.startElement(HTML.LABEL_ELEM, uiSelectOne);
        
        //renderRadio(facesContext,
        //            uiSelectOne,
        //            itemStrValue,
        //            selectItem.getLabel(),
        //            selectItem.isDisabled(),
        //            itemStrValue.equals(currentValue), false);
        //writer.endElement(HTML.LABEL_ELEM);

        //Render the radio component
        String itemId = renderRadio(
                facesContext,
                uiSelectOne,
                radio,
                itemStrValue,
                selectItem.isDisabled(),
                itemStrValue.equals(currentValue),
                false,
                index);
        
        //Render the
        // label element after the input
        boolean componentDisabled = isDisabled(facesContext, uiSelectOne);
        boolean itemDisabled = selectItem.isDisabled();
        boolean disabled = (componentDisabled || itemDisabled);

        renderLabel(writer, radio, uiSelectOne, itemId, selectItem, disabled);
    }
    
    protected String renderRadio(
            FacesContext facesContext,
            UISelectOne uiComponent, HtmlRadio radio, 
            String value, boolean disabled,
            boolean checked, boolean renderId, Integer itemNum)
            throws IOException
    {
        String clientId = uiComponent.getClientId(facesContext);

        String itemId = radio.isRenderLogicalId() ? 
                clientId + UINamingContainer.getSeparatorChar(facesContext) + itemNum :
                radio.getClientId(facesContext);

        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.INPUT_ELEM, uiComponent);
        
        // Force id rendering because it is necessary for the label
        // and for @this work correctly
        writer.writeAttribute(HTML.ID_ATTR, itemId, null);
        
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_RADIO, null);
        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        
        
        if (disabled)
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, HTML.DISABLED_ATTR, null);
        }

        if (checked)
        {
            writer.writeAttribute(HTML.CHECKED_ATTR, HTML.CHECKED_ATTR, null);
        }

        if (value != null)
        {
            writer.writeAttribute(HTML.VALUE_ATTR, value, null);
        }
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder
                && JavascriptUtils.isJavascriptAllowed(facesContext
                        .getExternalContext()))
        {
            behaviors = ((ClientBehaviorHolder) uiComponent)
                    .getClientBehaviors();

            renderBehaviorizedOnchangeEventHandler(facesContext, writer, radio, uiComponent, itemId, behaviors);
            renderBehaviorizedEventHandlers(facesContext,writer, radio, uiComponent, itemId, behaviors);
            renderBehaviorizedFieldEventHandlersWithoutOnchange(facesContext, writer, radio, uiComponent, itemId, behaviors);
            renderHTMLAttributes(writer, radio, uiComponent, HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
        }
        else
        {
            renderHTMLAttributes(writer, radio, uiComponent, HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        }
        
        if (isDisabled(facesContext, uiComponent))
        {
            writer.writeAttribute(
                    org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.DISABLED_ATTR,
                    Boolean.TRUE, null);
        }

        writer.endElement(HTML.INPUT_ELEM);

        return itemId;
    }
    
    public static void renderLabel(ResponseWriter writer, UIComponent radio,
            UIComponent component, String forClientId, SelectItem item,
            boolean disabled) throws IOException
    {
        writer.startElement(HTML.LABEL_ELEM, component);
        writer.writeAttribute(HTML.FOR_ATTR, forClientId, null);

        String labelClass = null;

        if (disabled)
        {
            labelClass = (String) radio.getAttributes().get(
                    JSFAttr.DISABLED_CLASS_ATTR);
            if (labelClass == null)
            {
                labelClass = (String) component.getAttributes().get(
                        JSFAttr.DISABLED_CLASS_ATTR);
            }
        }
        else
        {
            labelClass = (String) radio.getAttributes().get(
                    JSFAttr.ENABLED_CLASS_ATTR);
            if (labelClass == null)
            {
                labelClass = (String) component.getAttributes().get(
                        JSFAttr.ENABLED_CLASS_ATTR);
            }
        }
        if (labelClass != null)
        {
            writer.writeAttribute("class", labelClass, "labelClass");
        }

        if ((item.getLabel() != null) && (item.getLabel().length() > 0))
        {
            // writer.write(HTML.NBSP_ENTITY);
            writer.write(" ");
            if (item.isEscape())
            {
                //writer.write(item.getLabel());
                writer.writeText(item.getLabel(), null);
            }
            else
            {
                //writer.write(HTMLEncoder.encode (item.getLabel()));
                writer.write(item.getLabel());
            }
        }

        writer.endElement(HTML.LABEL_ELEM);
    }

    private static boolean renderHTMLAttributes(ResponseWriter writer,
            UIComponent radio, UIComponent selectOne, String[] attributes) throws IOException
    {
        boolean somethingDone = false;
        for (int i = 0, len = attributes.length; i < len; i++)
        {
            String attrName = attributes[i];
            Object value = radio.getAttributes().get(attrName);
            if (value == null)
            {
                value = selectOne.getAttributes().get(attrName);
            }
            if (HtmlRendererUtils.renderHTMLAttribute(writer, attrName, attrName, value ))
            {
                somethingDone = true;
            }
        }
        return somethingDone;
    }
    
    private static boolean renderBehaviorizedOnchangeEventHandler(
            FacesContext facesContext, ResponseWriter writer, UIComponent radio, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        boolean hasChange = HtmlRendererUtils.hasClientBehavior(ClientBehaviorEvents.CHANGE, clientBehaviors, facesContext);
        boolean hasValueChange = HtmlRendererUtils.hasClientBehavior(ClientBehaviorEvents.VALUECHANGE, clientBehaviors, facesContext);

        String value = (String) radio.getAttributes().get(HTML.ONCHANGE_ATTR);
        if (value == null)
        {
            value = (String) uiComponent.getAttributes().get(HTML.ONCHANGE_ATTR);
        }
        if (hasChange && hasValueChange) {
            String chain = HtmlRendererUtils.buildBehaviorChain(facesContext,
                    uiComponent, targetClientId, ClientBehaviorEvents.CHANGE, null, ClientBehaviorEvents.VALUECHANGE, null, clientBehaviors,
                    value, null);
            
            return HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR, chain);
        } else if (hasChange) {
            return HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent, targetClientId,
                    ClientBehaviorEvents.CHANGE, null, clientBehaviors, HTML.ONCHANGE_ATTR, value);
        } else if (hasValueChange) {
            return HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent, targetClientId,
                    ClientBehaviorEvents.VALUECHANGE, null, clientBehaviors, HTML.ONCHANGE_ATTR, value);
        } else {
            return HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR, value);
        }
    }
    
    private static void renderBehaviorizedEventHandlers(
            FacesContext facesContext, ResponseWriter writer, UIComponent radio, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.CLICK, clientBehaviors, HTML.ONCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.DBLCLICK, clientBehaviors, HTML.ONDBLCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEDOWN_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEDOWN, clientBehaviors, HTML.ONMOUSEDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEUP, clientBehaviors, HTML.ONMOUSEUP_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOVER_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEOVER, clientBehaviors, HTML.ONMOUSEOVER_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEMOVE_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEMOVE, clientBehaviors, HTML.ONMOUSEMOVE_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEOUT, clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYPRESS, clientBehaviors, HTML.ONKEYPRESS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYDOWN, clientBehaviors, HTML.ONKEYDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYUP, clientBehaviors, HTML.ONKEYUP_ATTR);
    }
    
    private static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer, UIComponent radio, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.FOCUS, clientBehaviors, HTML.ONFOCUS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.BLUR, clientBehaviors, HTML.ONBLUR_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR, radio, uiComponent, targetClientId,
                ClientBehaviorEvents.SELECT, clientBehaviors, HTML.ONSELECT_ATTR);
    }
    
    private static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent radio, UIComponent component, String targetClientId,
            String eventName, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName) throws IOException
    {
        String attributeValue = (String) radio.getAttributes().get(componentProperty);
        if (attributeValue == null)
        {
            attributeValue = (String) component.getAttributes().get(componentProperty);
        }
        return HtmlRendererUtils.renderBehaviorizedAttribute(
                facesContext, writer,
                componentProperty, component, targetClientId,
                eventName, null, clientBehaviors,
                htmlAttrName, attributeValue);
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


    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        if (uiComponent instanceof HtmlRadio)
        {
            HtmlRadio radio = (HtmlRadio) uiComponent;
            String forAttr = radio.getFor();
            if (forAttr == null)
            {
                throw new IllegalStateException("mandatory attribute 'for'");
            }
            int index = radio.getIndex();
            if (index < 0)
            {
                throw new IllegalStateException("positive index must be given");
            }

            UIComponent uiSelectOne = radio.findComponent(forAttr);
            if (uiSelectOne == null)
            {
                throw new IllegalStateException("Could not find component '" + forAttr + "' (calling findComponent on component '" + radio.getClientId(facesContext) + "')");
            }
            if (!(uiSelectOne instanceof UISelectOne))
            {
                throw new IllegalStateException("UISelectOne expected");
            }

            if (uiSelectOne instanceof ClientBehaviorHolder) {
                ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) uiSelectOne;

                Map<String, List<ClientBehavior>> clientBehaviors =
                        clientBehaviorHolder.getClientBehaviors();

                if (clientBehaviors != null && !clientBehaviors.isEmpty()) {
                    Map<String, String> paramMap = facesContext.getExternalContext().
                            getRequestParameterMap();

                    String behaviorEventName = paramMap.get("jakarta.faces.behavior.event");

                    if (behaviorEventName != null) {
                        List<ClientBehavior> clientBehaviorList = clientBehaviors.get(behaviorEventName);

                        if (clientBehaviorList != null && !clientBehaviorList.isEmpty()) {
                            String clientId = paramMap.get("jakarta.faces.source");

                            if (radio.getClientId().equals(clientId)) {
                                for (ClientBehavior clientBehavior : clientBehaviorList) {
                                    clientBehavior.decode(facesContext, radio);
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            super.decode(facesContext, uiComponent);
        }
    }

}
