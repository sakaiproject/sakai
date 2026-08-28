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
package org.apache.myfaces.custom.inputHtml;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.custom.tabbedpane.HtmlPanelTab;
import org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.HTMLEncoder;

/**
 * 
 * @author Sylvain Vieujot (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (jue, 03 jul 2008) $
 */
@JSFRenderer(
   renderKitId = "HTML_BASIC", 
   family = "jakarta.faces.Input",
   type = "org.apache.myfaces.InputHtml")
public class InputHtmlRenderer extends HtmlRenderer{
    // TODO : Enable multiple editors on one page
    // TODO : Fix i18n (check the kupustart_form.js)
    // TODO : Finish Disabled mode.
    // TODO : Automatic Fallback for non kupu capable browsers (Safari, smartphones, non javascript, ...).
    // TODO : Make Image & Link Library work.
    // TODO : Allow disabeling of tag filtering
    // TODO : Fix height while zoomed

    private static final Log log = LogFactory.getLog(HtmlRendererUtils.class);
    
    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent) {
        if( !UserRoleUtils.isEnabledOnUserRole(uiComponent) )
            return false;

        return ((InputHtml)uiComponent).isDisabled();
    }

    private static boolean useFallback(InputHtml editor){
        // TODO : Handle fallback="auto"
        return editor.getFallback().equals("true");
    }

    public boolean getRendersChildren()
    {
        return true;
    }

    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
    }

    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException
    {
    }

    public void encodeEnd(FacesContext context, UIComponent uiComponent) throws IOException {
        RendererUtils.checkParamValidity(context, uiComponent, InputHtml.class);
        
        UIComponent compositeFacet = (UIComponent) uiComponent
                .getFacet(UIComponent.COMPOSITE_FACET_NAME);

        if (compositeFacet == null)
        {
            if (log.isErrorEnabled())
            {
                log
                        .error("facet UIComponent.COMPOSITE_FACET_NAME not found when rendering composite component "
                                + uiComponent.getClientId(context));
            }
            return;
        }
        InputHtml editor = (InputHtml) uiComponent;
        if( HtmlRendererUtils.isDisplayValueOnly(editor) )
            encodeDisplayValueOnly(context, editor);
        else if( useFallback(editor) )
            encodeEndFallBackMode(context, editor);
        else{
            // HACK
            // As only one inputHtml is supported by page in this version,
            // we need to make sure we don't encode hidden inputHtml
            // TODO : Fix this by supporting multiple inputHtml per page.

            if( ! isVisible(editor) ){
                encodeHidden(context, editor);
            }else if( ! hasThisPageAlreadyRenderedAnInputHtml( context ) ){
                compositeFacet.encodeAll(context);
                setThisPageAlreadyRenderedAnInputHtml( context );
            }else{
                log.warn("Only one inputHtml can be displayed at the same time. The component will be rendered isung a textarea." +
                         "\nConpoment : "+RendererUtils.getPathToComponent( editor ));
                encodeEndFallBackMode(context, editor);
            }
        }
    }

    static private boolean hasThisPageAlreadyRenderedAnInputHtml(FacesContext context){
            return context.getExternalContext().getRequestMap().containsKey( InputHtmlRenderer.class.getName() );
    }

    static private void setThisPageAlreadyRenderedAnInputHtml(FacesContext context){
        context.getExternalContext().getRequestMap().put(InputHtmlRenderer.class.getName(), Boolean.TRUE);
    }

    /**
     * Try to figure out if this component is visible to avoid rendering the code if not necessary.
     */
    private boolean isVisible(InputHtml editor){
            for(UIComponent parent = editor.getParent(); parent != null ; parent = parent.getParent()){
                if( parent instanceof HtmlPanelTab ){
                    HtmlPanelTab panelTab = (HtmlPanelTab) parent;
                    HtmlPanelTabbedPane panelTabbedPane = null;
                    for(UIComponent panelAncestor=panelTab.getParent(); panelAncestor!=null ; panelAncestor=panelAncestor.getParent()){
                        if( panelAncestor instanceof HtmlPanelTabbedPane ){
                            panelTabbedPane = (HtmlPanelTabbedPane)panelAncestor;
                            break;
                        }
                    }

                    if( panelTabbedPane != null ){
                    if( panelTabbedPane.isClientSide() ){
                        parent = panelTabbedPane;
                        continue;
                    }

                    // Not client side tabbed pane.
                    // We need to check if the current panel tab is bisible;
                    int selectedIndex = panelTabbedPane.getSelectedIndex();
                    List children = panelTabbedPane.getChildren();
                    int tabIdx = 0;
                    for (int i = 0, len = children.size(); i < len && tabIdx <= selectedIndex ; i++){
                        UIComponent child = htmlTabbedPaneRenderer_getUIComponent((UIComponent)children.get(i));
                        if (child instanceof HtmlPanelTab){
                                if( child == panelTab ){
                                    if( ! child.isRendered() || tabIdx != selectedIndex){
                                    return false;
                                }else{
                                        // It's visible. Check at upper level.
                                        parent = panelTabbedPane;
                                    continue;
                                }
                                }
                            tabIdx++;
                        }
                        }
                    }else{
                        log.debug("pannelTabbedPane == null for component "+RendererUtils.getPathToComponent(panelTab));
                    }
                }
            }

            return true;
    }

    /**
     * This is a copy of HtmlTabbedPaneRenderer.getUIComponent
     */
    private UIComponent htmlTabbedPaneRenderer_getUIComponent(UIComponent uiComponent)
    {
        /* todo: forms other than UIForm, e.g. Trinidad's UIForm */
        if (uiComponent instanceof UIForm || uiComponent instanceof UINamingContainer)
        {
            List children = uiComponent.getChildren();
            for (int i = 0, len = children.size(); i < len; i++)
            {
                uiComponent = htmlTabbedPaneRenderer_getUIComponent((UIComponent)children.get(i));
            }
        }
        return uiComponent;
    }

    private void encodeHidden(FacesContext context, InputHtml editor) throws IOException {
        String clientId = editor.getClientId(context);
        // Use a hidden textarea
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTML.TEXTAREA_ELEM, editor);

        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        writer.writeAttribute(HTML.STYLE_ATTR, "display:none", null);

        String text = RendererUtils.getStringValue(context, editor);
        writer.writeText(text, JSFAttr.VALUE_ATTR);

        writer.endElement(HTML.TEXTAREA_ELEM);
    }

    private void encodeDisplayValueOnly(FacesContext context, InputHtml editor) throws IOException {
        // Use only a textarea
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTML.SPAN_ELEM, editor);
        HtmlRendererUtils.writeIdIfNecessary(writer, editor, context);

        HtmlRendererUtils.renderDisplayValueOnlyAttributes(editor, writer);

        String text = RendererUtils.getStringValue(context, editor);
        writer.write( editor.getHtmlBody( text ) );

        writer.endElement(HTML.SPAN_ELEM);
    }

    private void encodeEndFallBackMode(FacesContext context, InputHtml editor) throws IOException {
        String clientId = editor.getClientId(context);
        // Use only a textarea
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTML.TEXTAREA_ELEM, editor);

        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        HtmlRendererUtils.writeIdIfNecessary(writer, editor, context);

        if( editor.getStyle()!=null )
            writer.writeAttribute(HTML.STYLE_ATTR, editor.getStyle(), null);
        if( editor.getStyleClass()!=null )
            writer.writeAttribute(HTML.CLASS_ATTR, editor.getStyleClass(), null);

        if (isDisabled(context, editor))
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);

        String text = RendererUtils.getStringValue(context, editor);
        writer.write( htmlToPlainText(text, editor) );

        writer.endElement(HTML.TEXTAREA_ELEM);
    }

    private static String htmlToPlainText(String html, InputHtml editor){
        return editor.getHtmlBody( html )
                .replaceAll("<br.*>","\n")
                .replaceAll("<.+?>", "");
    }

    public void decode(FacesContext facesContext, UIComponent uiComponent) {
        RendererUtils.checkParamValidity(facesContext, uiComponent, InputHtml.class);
        InputHtml editor = (InputHtml) uiComponent;

        Map paramMap = facesContext.getExternalContext().getRequestParameterMap();
        String clientId = uiComponent.getClientId(facesContext);

        if (paramMap.containsKey(clientId)) {
            //request parameter found, set submittedValue
            String submitedText = (String)paramMap.get(clientId);
            String htmlText = useFallback(editor) ? HTMLEncoder.encode(submitedText, true, true) : submitedText;

            editor.setSubmittedValue( htmlText );
        } else {
            log.warn(HtmlRendererUtils.NON_SUBMITTED_VALUE_WARNING + " Component : "+
                     RendererUtils.getPathToComponent( editor ));
        }
    }

    public Object getConvertedValue(FacesContext facesContext, UIComponent uiComponent, Object submittedValue) throws ConverterException {
        RendererUtils.checkParamValidity(facesContext, uiComponent, InputHtml.class);
        InputHtml editor = (InputHtml) uiComponent;
        String submittedDocument = editor.getValueFromDocument((String)submittedValue);
        return RendererUtils.getConvertedUIOutputValue(facesContext, editor, submittedDocument);
    }
}
