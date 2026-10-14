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
import java.util.List;
import java.util.Map;

import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;

import org.apache.myfaces.renderkit.html.util.DummyFormUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.CollapsiblePanel"
 *   
 * @author Kalle Korhonen (latest modification by $Author: lu4242 $)
 * @version $Revision: 671709 $ $Date: 2008-06-25 22:12:59 -0500 (mié, 25 jun 2008) $
 */
public class HtmlCollapsiblePanelRenderer extends HtmlRenderer {
    //private static final Log log = LogFactory.getLog(HtmlCollapsiblePanel.class);
    private static final String LINK_ID = "ToggleCollapsed".intern();
    private static final String COLLAPSED_STATE_ID = "CollapsedState".intern();

    public boolean getRendersChildren() {
        return true;
    }

    public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        // RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlCollapsiblePanel.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        HtmlCollapsiblePanel collapsiblePanel = (HtmlCollapsiblePanel) uiComponent;

        UIComponent headerComp = collapsiblePanel.getFacet("header");

        if (headerComp == null){
            HtmlCommandLink link = getLink(facesContext, collapsiblePanel);
            collapsiblePanel.getChildren().add(link);

            headerComp = link;
        }

        //Render the current state - collapsed or not - of the panel.
        HtmlRendererUtils.renderHiddenInputField(writer, collapsiblePanel.getClientId(facesContext) +
            COLLAPSED_STATE_ID,
                                                 collapsiblePanel.getSubmittedValue() != null ?
                                                     collapsiblePanel.getSubmittedValue() : (collapsiblePanel.isCollapsed() + ""));

        // Always render the header - to be able toggle the collapsed state
        RendererUtils.renderChild(facesContext, headerComp);
        headerComp.setRendered(false);

        // conditionally render the rest of the children
        if (!collapsiblePanel.isCollapsed()) {
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            // TODO apply styles from the parent element to this DIV
            writer.startElement(HTML.DIV_ELEM, uiComponent);
            RendererUtils.renderChildren(facesContext, uiComponent);
            writer.endElement(HTML.DIV_ELEM);
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        }
        else {
            UIComponent component = collapsiblePanel.getFacet("closedContent");
            if (component != null) {
                writer.startElement(HTML.DIV_ELEM, uiComponent);
                RendererUtils.renderChild(facesContext, component);
                writer.endElement(HTML.DIV_ELEM);
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            }
        }

        headerComp.setRendered(true);
    }

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlCollapsiblePanel.class);
        ResponseWriter writer = facesContext.getResponseWriter();

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.DIV_ELEM, uiComponent);

        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            writer.writeAttribute(HTML.ID_ATTR, uiComponent.getClientId(facesContext), null);
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.UNIVERSAL_ATTRIBUTES);
            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
        }
        
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        viewHandler.getActionURL(facesContext, viewId);

        facesContext.getApplication();
    }


    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        //RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlCollapsiblePanel.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.endElement(HTML.DIV_ELEM);
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
    }

    public void decode(FacesContext facesContext, UIComponent uiComponent) {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlCollapsiblePanel.class);
        HtmlCollapsiblePanel collapsiblePanel = (HtmlCollapsiblePanel) uiComponent;

        Map reqParams = facesContext.getExternalContext().getRequestParameterMap();

        String togglingIndicated = (String) reqParams.get(HtmlRendererUtils
                .getHiddenCommandLinkFieldName(
                DummyFormUtils.findNestingForm(collapsiblePanel, facesContext)));
        // if togglingIndicated is null this application could be running within the RI.
        // The RI denotes link activation by adding a hidden field with the name
        // and value of the link client ID.
        if (togglingIndicated == null
                && reqParams.containsKey(collapsiblePanel.getClientId(facesContext) + LINK_ID)) {
            togglingIndicated = collapsiblePanel.getClientId(facesContext) + LINK_ID;
        }

        String reqValue = (String) reqParams.get(
            collapsiblePanel.getClientId(facesContext) + COLLAPSED_STATE_ID);

        collapsiblePanel.setCurrentlyCollapsed(HtmlCollapsiblePanel.isCollapsed(reqValue));

        if ((collapsiblePanel.getClientId(facesContext) + LINK_ID).equals(togglingIndicated)) {
            if (reqValue != null)
            {
                collapsiblePanel.setSubmittedValue("" + !collapsiblePanel.isCurrentlyCollapsed());
            }
            else
            {
                collapsiblePanel.setSubmittedValue("" + !collapsiblePanel.isCollapsed());
            }
            
            UIComponent header = collapsiblePanel.getFacet("header");
            
            if (header != null)
            {
                UICommand link = (UICommand)RendererUtils.findComponent(header,HtmlHeaderLink.class);
                
                if (link != null && link.isImmediate())
                {
                    //In this case we need to update the model directly, because
                    //PROCESS_VALIDATIONS and UPDATE_MODEL phase is not called
                    //(immediate=true), but we need to reflect the change
                    //on the collapsed value.
                    //In this case, no ValueChangeEvent is fired,
                    //because it is an immediate call.
                    Object convertedValue = getConvertedValue(facesContext,collapsiblePanel,
                            collapsiblePanel.getSubmittedValue());
                    
                    collapsiblePanel.setValue(convertedValue);
                    collapsiblePanel.setSubmittedValue(null);
                    collapsiblePanel.updateModel(facesContext);
                }
            }
        }
        else {
            if (reqValue != null)
                collapsiblePanel.setSubmittedValue("" + collapsiblePanel.isCurrentlyCollapsed());
        }
        
        HtmlRendererUtils.decodeClientBehaviors(facesContext, uiComponent);
    }

    protected HtmlCommandLink getLink(FacesContext facesContext, HtmlCollapsiblePanel collapsiblePanel)
        throws IOException {
        Application application = facesContext.getApplication();
        HtmlCommandLink link = (HtmlCommandLink) application.createComponent(HtmlCommandLink.COMPONENT_TYPE);
        link.setId(collapsiblePanel.getId() + LINK_ID);
        link.setTransient(true);
        link.setImmediate(true);

        List<UIComponent> children = link.getChildren();
        // Create the indicator. You could later make this conditional and render optional images instead
        HtmlOutputText uiText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        uiText.setTransient(true);
        uiText.setValue(collapsiblePanel.isCollapsed() ? "&gt;" : "&#957;");
        uiText.setEscape(false);
        uiText.setStyleClass(collapsiblePanel.getIndicatorStyleClass());
        uiText.setStyle(collapsiblePanel.getIndicatorStyle());
        children.add(uiText);

        // Create the optional label
        String label = collapsiblePanel.getTitle();
        if (label != null) {
            uiText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            uiText.setTransient(true);
            uiText.setValue(" " + label);
            uiText.setStyleClass(collapsiblePanel.getTitleStyleClass());
            uiText.setStyle(collapsiblePanel.getTitleStyle());
            children.add(uiText);
        }
        return link;
    }

    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        if (submittedValue instanceof String) {
            return Boolean.valueOf((String) submittedValue);
        }

        return super.getConvertedValue(context, component, submittedValue);
    }
}
