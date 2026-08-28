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
package org.apache.myfaces.custom.popup;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.shared_tomahawk.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.Popup"
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
@ResourceDependency(library="oam.custom.popup",name="JSPopup.js")
public class HtmlPopupRenderer
    extends HtmlRenderer
{
    public static final String RENDERER_TYPE = "org.apache.myfaces.Popup";
    //private static final Log log = LogFactory.getLog(HtmlListRenderer.class);
    
    private static final String LAYOUT_BLOCK = "block";
    private static final String LAYOUT_DIV = "div";
    private static final String LAYOUT_SPAN = "span";
    private static final String LAYOUT_NONE = "none";

    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);
        
        HtmlRendererUtils.decodeClientBehaviors(context, component);
    }

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
    }

    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException
    {

    }


    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlPopup.class);

        HtmlPopup popup = (HtmlPopup) uiComponent;

        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }
        
        UIComponent popupFacet = popup.getPopup();

        String popupId = writePopupScript(
                facesContext, popup.getClientId(facesContext),
                popup.getDisplayAtDistanceX(),popup.getDisplayAtDistanceY(), uiComponent);

        //writeMouseOverAndOutAttribs(popupId, popupFacet.getChildren());

        ResponseWriter writer = facesContext.getResponseWriter();
        
        String layout = popup.getLayout();
        
        if (LAYOUT_BLOCK.equals(layout) || LAYOUT_DIV.equals(layout))
        {
            writer.startElement(HTML.DIV_ELEM, popup);
            writer.writeAttribute(HTML.ONMOUSEOVER_ATTR, popupId + ".display(event);", null);
            writer.writeAttribute(HTML.ONMOUSEOUT_ATTR, popupId + ".hide(event);", null);
        }
        else if (LAYOUT_NONE.equals(layout))
        {
            writeMouseOverAttribs(popupId, uiComponent.getChildren(),
                    popup.getClosePopupOnExitingElement()==null ||
                            popup.getClosePopupOnExitingElement().booleanValue());
        }
        else
        {
            writer.startElement(HTML.SPAN_ELEM, popup);
            writer.writeAttribute(HTML.ONMOUSEOVER_ATTR, popupId + ".display(event);", null);
            writer.writeAttribute(HTML.ONMOUSEOUT_ATTR, popupId + ".hide(event);", null);
        }
        
        RendererUtils.renderChildren(facesContext, uiComponent);
        
        if (LAYOUT_BLOCK.equals(layout) || LAYOUT_DIV.equals(layout))
        {
            writer.endElement(HTML.DIV_ELEM);
        }
        else if (LAYOUT_NONE.equals(layout))
        {
        }
        else
        {
            writer.endElement(HTML.SPAN_ELEM);
        }

        writer.startElement(HTML.DIV_ELEM, popup);
        writer.writeAttribute(HTML.STYLE_ATTR,(popup.getStyle()!=null?(popup.getStyle()+
                (popup.getStyle().trim().endsWith(";")?"":";")):"")+
                "position:absolute;display:none;",null);
        if(popup.getStyleClass()!=null)
        {
            writer.writeAttribute(HTML.CLASS_ATTR,popup.getStyleClass(),null);
        }
        writer.writeAttribute(HTML.ID_ATTR, popup.getClientId(facesContext),null);
        if (behaviors != null && !behaviors.isEmpty())
        {
            HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEOVER_ATTR, popup, ClientBehaviorEvents.MOUSEOVER, null, behaviors,
                    HTML.ONMOUSEOVER_ATTR, popup.getOnmouseover(), new String(popupId+".redisplay();"));
            
            Boolean closeExitPopup = popup.getClosePopupOnExitingPopup();

            if(closeExitPopup==null || closeExitPopup.booleanValue())
            {
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer,
                        HTML.ONMOUSEOUT_ATTR, popup, ClientBehaviorEvents.MOUSEOUT, null, behaviors,
                        HTML.ONMOUSEOUT_ATTR, popup.getOnmouseover(), popupId + ".hide();");
            }
            else
            {
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer,
                        HTML.ONMOUSEOUT_ATTR, popup, ClientBehaviorEvents.MOUSEOUT, null, behaviors,
                        HTML.ONMOUSEOUT_ATTR, popup.getOnmouseover(), null);
            }
            
            HtmlRendererUtils.renderBehaviorizedEventHandlersWithoutOnmouseoverAndOnmouseout(facesContext, writer, uiComponent, behaviors);
        }
        else
        {
            writer.writeAttribute(HTML.ONMOUSEOVER_ATTR, new String(popupId+".redisplay();"),null);

            Boolean closeExitPopup = popup.getClosePopupOnExitingPopup();

            if(closeExitPopup==null || closeExitPopup.booleanValue())
                writer.writeAttribute(HTML.ONMOUSEOUT_ATTR, popupId + ".hide();",null);
            
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONCLICK_ATTR, HTML.ONCLICK_ATTR, popup.getOnclick());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONDBLCLICK_ATTR, HTML.ONDBLCLICK_ATTR, popup.getOndblclick());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONMOUSEDOWN_ATTR, HTML.ONMOUSEDOWN_ATTR, popup.getOnmousedown());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONMOUSEUP_ATTR, HTML.ONMOUSEUP_ATTR, popup.getOnmouseup());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONMOUSEMOVE_ATTR, HTML.ONMOUSEMOVE_ATTR, popup.getOnmousemove());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONKEYPRESS_ATTR, HTML.ONKEYPRESS_ATTR, popup.getOnkeypress());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONKEYDOWN_ATTR, HTML.ONKEYDOWN_ATTR, popup.getOnkeydown());
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONKEYUP_ATTR, HTML.ONKEYUP_ATTR, popup.getOnkeyup());
        }
        
        RendererUtils.renderChild(facesContext, popupFacet);
        writer.endElement(HTML.DIV_ELEM);
    }

    private void writeMouseOverAttribs(String popupId, List children, boolean renderMouseOut)
    {
        for (int i = 0; i < children.size(); i++)
        {
            UIComponent uiComponent = (UIComponent) children.get(i);

            callMethod(uiComponent,"onmouseover", popupId + ".display(event);");

            if(renderMouseOut)
                callMethod(uiComponent,"onmouseout", popupId + ".hide(event);");

            writeMouseOverAttribs(popupId, uiComponent.getChildren(),renderMouseOut);
        }
    }

    private String writePopupScript(FacesContext context, String clientId,
                                    Integer displayAtDistanceX, Integer displayAtDistanceY, UIComponent uiComponent)
        throws IOException
    {
        //AddResourceFactory.getInstance(context).addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlPopupRenderer.class, "JSPopup.js");

        String popupId = JavascriptUtils.getValidJavascriptName(clientId+"Popup",false);

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTML.SCRIPT_ELEM, uiComponent);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR,HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT,null);
        writer.writeText("var "+popupId+"=new orgApacheMyfacesPopup('"+clientId+"',"+
                (displayAtDistanceX==null?-5:displayAtDistanceX.intValue())+","+
                (displayAtDistanceY==null?-5:displayAtDistanceY.intValue())+");",null);
        writer.endElement(HTML.SCRIPT_ELEM);

        return popupId;
    }


    private void callMethod(UIComponent uiComponent, String propName, String value)
    {
        Object oldValue = uiComponent.getAttributes().get(propName);

        String oldValueStr = "";

        String genCommentary = "/* generated code */";

        if(oldValue != null)
        {
            oldValueStr = oldValue.toString().trim();

            int genCommentaryIndex;

            //check if generated code has already been added...
            if((genCommentaryIndex=oldValueStr.indexOf(genCommentary))!=-1)
            {
                oldValueStr = oldValueStr.substring(0,genCommentaryIndex);
            }

            if(oldValueStr.length()>0 && !oldValueStr.endsWith(";"))
                oldValueStr +=";";
        }

        value = oldValueStr+genCommentary+value;

        uiComponent.getAttributes().put(propName, value);
    }
}
