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
package org.apache.myfaces.custom.tabbedpane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.TabbedPane"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 783584 $ $Date: 2009-06-10 19:00:13 -0500 (mié, 10 jun 2009) $
 */
@ResourceDependency(library="oam.custom.tabbedpane",name="defaultStyles.css")
@ListenerFor(systemEventClass=PreRenderViewAddResourceEvent.class)
public class HtmlTabbedPaneRenderer
        extends HtmlRenderer implements ComponentSystemEventListener
{
    private static final String HEADER_ROW_CLASS = "myFaces_pannelTabbedPane_HeaderRow";
    private static final String ACTIVE_HEADER_CELL_CLASS = "myFaces_panelTabbedPane_activeHeaderCell";
    private static final String INACTIVE_HEADER_CELL_CLASS = "myFaces_panelTabbedPane_inactiveHeaderCell";
    private static final String DISABLED_HEADER_CELL_CLASS = "myFaces_panelTabbedPane_disabledHeaderCell";
    private static final String EMPTY_HEADER_CELL_CLASS = "myFaces_panelTabbedPane_emptyHeaderCell";
    private static final String SUB_HEADER_ROW_CLASS = "myFaces_pannelTabbedPane_subHeaderRow";
    private static final String SUB_HEADER_CELL_CLASS = "myFaces_panelTabbedPane_subHeaderCell";
    private static final String SUB_HEADER_CELL_CLASS_ACTIVE = "myFaces_panelTabbedPane_subHeaderCell_active";
    private static final String SUB_HEADER_CELL_CLASS_INACTIVE = "myFaces_panelTabbedPane_subHeaderCell_inactive";
    private static final String SUB_HEADER_CELL_CLASS_FIRST = "myFaces_panelTabbedPane_subHeaderCell_first";
    private static final String SUB_HEADER_CELL_CLASS_LAST = "myFaces_panelTabbedPane_subHeaderCell_last";
    private static final String CONTENT_ROW_CLASS = "myFaces_panelTabbedPane_contentRow";
    private static final String TAB_PANE_CLASS = "myFaces_panelTabbedPane_pane";

    private static final String DEFAULT_BG_COLOR = "white";

    private static final String AUTO_FORM_SUFFIX = ".autoform";
    private static final String TAB_DIV_SUFFIX = ".content";

    public void processEvent(ComponentSystemEvent event)
    {
        HtmlPanelTabbedPane tabbedPane = (HtmlPanelTabbedPane)event.getComponent();
        if( tabbedPane.isClientSide() )
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            TomahawkResourceUtils.addOutputScriptResource(facesContext,
                    "oam.custom.tabbedpane",
                    "dynamicTabs.js");

            TomahawkResourceUtils.addInlineOutputStylesheetResource(facesContext,
                    '#'+getTableStylableId(tabbedPane,facesContext)+" ."+ACTIVE_HEADER_CELL_CLASS+" input,\n" +
                    '#'+getTableStylableId(tabbedPane,facesContext)+" ."+TAB_PANE_CLASS+",\n" +
                    '#'+getTableStylableId(tabbedPane,facesContext)+" ."+SUB_HEADER_CELL_CLASS+"{\n"+
                    "background-color:" + tabbedPane.getBgcolor()+";\n"+
                    "}\n");
        }
    }

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        // NoOp
    }

    public boolean getRendersChildren()
    {
        return true;
    }

    public void encodeChildren(FacesContext facescontext, UIComponent uicomponent) throws IOException
    {
        // NoOp
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlPanelTabbedPane.class);
        HtmlPanelTabbedPane tabbedPane = (HtmlPanelTabbedPane)uiComponent;
        if (tabbedPane.getBgcolor() == null)
        {
            tabbedPane.setBgcolor(DEFAULT_BG_COLOR);
        }

        //AddResource addResource = AddResourceFactory.getInstance(facesContext);

        //addResource.addStyleSheet(facesContext,AddResource.HEADER_BEGIN,
        //                          HtmlTabbedPaneRenderer.class, "defaultStyles.css");

        //if( tabbedPane.isClientSide() ){
        //        addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, HtmlTabbedPaneRenderer.class, "dynamicTabs.js");
        //        addResource.addInlineStyleAtPosition(facesContext,AddResource.HEADER_BEGIN,
        //                                             '#'+getTableStylableId(tabbedPane,facesContext)+" ."+ACTIVE_HEADER_CELL_CLASS+" input,\n" +
        //                                             '#'+getTableStylableId(tabbedPane,facesContext)+" ."+TAB_PANE_CLASS+",\n" +
        //                                             '#'+getTableStylableId(tabbedPane,facesContext)+" ."+SUB_HEADER_CELL_CLASS+"{\n"+
        //                                             "background-color:" + tabbedPane.getBgcolor()+";\n"+
        //                                             "}\n");
        //}


        ResponseWriter writer = facesContext.getResponseWriter();

        Map<String, List<ClientBehavior>> behaviors = tabbedPane.getClientBehaviors();
        if (!behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
        }
        
        for (UIComponent child : tabbedPane.getChildren())
        {
            if (child instanceof HtmlPanelTab)
            {
                Map<String, List<ClientBehavior>> bh = ((HtmlPanelTab) child).getClientBehaviors();
                if (!bh.isEmpty())
                {
                    ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
                }
            }
        }

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        int selectedIndex = tabbedPane.getSelectedIndex();

        FormInfo parentFormInfo = RendererUtils.findNestingForm(tabbedPane, facesContext);
        if (parentFormInfo == null)
        {
            writeFormStart(writer, facesContext, tabbedPane);
        }

        List children = tabbedPane.getChildren();

        if( tabbedPane.isClientSide() ){
            List headerIDs = new ArrayList();
            List tabIDs = new ArrayList();
            for (int i = 0, len = children.size(); i < len; i++)
            {
                UIComponent child = getUIComponent((UIComponent)children.get(i));
                if (child instanceof HtmlPanelTab && child.isRendered()){
                    HtmlPanelTab tab = (HtmlPanelTab) child;
                    tabIDs.add( child.getClientId(facesContext) + TAB_DIV_SUFFIX);
                    if( ! isDisabled(facesContext, tab) )
                        headerIDs.add( getHeaderCellID(tab, facesContext) );
                }
            }

            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.startElement(HTML.SCRIPT_ELEM, tabbedPane);
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
            writer.write('\n');

            writer.write( getHeaderCellsIDsVar(tabbedPane,facesContext)+"= new Array(" );
            for(Iterator ids=headerIDs.iterator(); ids.hasNext();){
                String id = (String)ids.next();
                writer.write('"'+JavascriptUtils.encodeString( id )+'"');
                if( ids.hasNext() )
                    writer.write(',');
            }
            writer.write( ");\n" ); // end Array

            writer.write( getTabsIDsVar(tabbedPane,facesContext)+"= new Array(" );
            for(Iterator ids=tabIDs.iterator(); ids.hasNext();){
                String id = (String)ids.next();
                writer.write('"'+JavascriptUtils.encodeString( id )+'"');
                if( ids.hasNext() )
                    writer.write(',');
            }
            writer.write( ");\n" ); // end Array

            writer.endElement(HTML.SCRIPT_ELEM);
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);

            String submitFieldIDAndName = getTabIndexSubmitFieldIDAndName(tabbedPane, facesContext);
            writer.startElement(HTML.INPUT_ELEM, tabbedPane);
            writer.writeAttribute(HTML.ID_ATTR, submitFieldIDAndName, null);
            writer.writeAttribute(HTML.NAME_ATTR, submitFieldIDAndName, null);
            writer.writeAttribute(HTML.STYLE_ATTR, "display:none", null);
            writer.endElement(HTML.INPUT_ELEM);
        }

        writeTableStart(writer, facesContext, tabbedPane);
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TR_ELEM, tabbedPane);
        writer.writeAttribute(HTML.CLASS_ATTR, HEADER_ROW_CLASS, null);

        //Tab headers
        int tabIdx = 0;
        int visibleTabCount = 0;
        int visibleTabSelectedIdx = -1;
        for (int i = 0, len = children.size(); i < len; i++)
        {
            UIComponent child = getUIComponent((UIComponent)children.get(i));
            if (child instanceof HtmlPanelTab)
            {
                if (child.isRendered())
                {
                    writeHeaderCell(writer,
                                    facesContext,
                                    tabbedPane,
                                    (HtmlPanelTab)child,
                                    tabIdx,
                                    visibleTabCount,
                                    tabIdx == selectedIndex,
                                    isDisabled(facesContext, (HtmlPanelTab)child));
                    if (tabIdx == selectedIndex)
                    {
                        visibleTabSelectedIdx = visibleTabCount;
                    }
                    visibleTabCount++;
                }
                tabIdx++;
            }
        }

        //Empty tab cell on the right for better look
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        HtmlRendererUtils.writePrettyIndent(facesContext);
        writer.startElement(HTML.TD_ELEM, tabbedPane);
        writer.writeAttribute(HTML.CLASS_ATTR, EMPTY_HEADER_CELL_CLASS, null);
        writer.write("&#160;");
        writer.endElement(HTML.TD_ELEM);
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.endElement(HTML.TR_ELEM);

        //Sub header cells
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TR_ELEM, tabbedPane);
        writer.writeAttribute(HTML.CLASS_ATTR, SUB_HEADER_ROW_CLASS, null);
        writeSubHeaderCells(writer, facesContext, tabbedPane, visibleTabCount, visibleTabSelectedIdx);
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.endElement(HTML.TR_ELEM);

        //Tabs
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TR_ELEM, tabbedPane);
        writer.writeAttribute(HTML.CLASS_ATTR, CONTENT_ROW_CLASS, null);
        writer.startElement(HTML.TD_ELEM, tabbedPane);
        writer.writeAttribute(HTML.COLSPAN_ATTR, Integer.toString(visibleTabCount + 1), null);
        String tabContentStyleClass = tabbedPane.getTabContentStyleClass();
        writer.writeAttribute(HTML.CLASS_ATTR, TAB_PANE_CLASS+(tabContentStyleClass==null ? "" : " "+tabContentStyleClass), null);

        writeTabsContents(writer, facesContext, tabbedPane, selectedIndex);

        writer.endElement(HTML.TD_ELEM);
        writer.endElement(HTML.TR_ELEM);

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.endElement(HTML.TABLE_ELEM);

        if (parentFormInfo == null)
        {
            writeFormEnd(writer, facesContext);
        }
    }


    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlPanelTabbedPane.class);

        HtmlPanelTabbedPane tabbedPane = (HtmlPanelTabbedPane)uiComponent;

        Map paramMap = facesContext.getExternalContext().getRequestParameterMap();

        int tabIdx = 0;
        List children = tabbedPane.getChildren();
        for (int i = 0, len = children.size(); i < len; i++)
        {
            UIComponent child = getUIComponent((UIComponent)children.get(i));
            if (child instanceof HtmlPanelTab)
            {
                String paramName = tabbedPane.getClientId(facesContext) + "." + tabIdx;
                String paramValue = (String)paramMap.get(paramName);
                if (paramValue != null && paramValue.length() > 0)
                {
                    tabbedPane.queueEvent(new TabChangeEvent(tabbedPane,
                                                             tabbedPane.getSelectedIndex(),
                                                             tabIdx));
                    return;
                }
                tabIdx++;
            }
        }

        // No request due to a header button pressed.
        // Restore a client-side switch
        if( tabbedPane.isClientSide() ){
            String clientSideIndex = (String)paramMap.get(getTabIndexSubmitFieldIDAndName(tabbedPane, facesContext));
            if (clientSideIndex != null && clientSideIndex.length() > 0)
            {
                tabbedPane.setSelectedIndex( Integer.parseInt(clientSideIndex) );
                return;
            }
        }
    }

    protected void writeFormStart(ResponseWriter writer,
                                  FacesContext facesContext,
                                  UIComponent tabbedPane)
        throws IOException
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        String actionURL = viewHandler.getActionURL(facesContext, viewId);

        //write out auto form
        writer.startElement(HTML.FORM_ELEM, tabbedPane);
        writer.writeAttribute(HTML.NAME_ATTR, tabbedPane.getClientId(facesContext) + AUTO_FORM_SUFFIX, null);
        writer.writeAttribute(HTML.STYLE_ATTR, "display:inline", null);
        writer.writeAttribute(HTML.METHOD_ATTR, "post", null);
        writer.writeURIAttribute(HTML.ACTION_ATTR,
                                 facesContext.getExternalContext().encodeActionURL(actionURL),
                                 null);
        writer.flush();
    }

    protected String getHeaderCellsIDsVar(HtmlPanelTabbedPane tabbedPane, FacesContext facesContext){
        return JavascriptUtils.getValidJavascriptName(
                "panelTabbedPane_"+tabbedPane.getClientId(facesContext)+"_HeadersIDs",
                false);
    }

    protected String getTabsIDsVar(HtmlPanelTabbedPane tabbedPane, FacesContext facesContext){
        return JavascriptUtils.getValidJavascriptName(
                "panelTabbedPane_"+tabbedPane.getClientId(facesContext)+"_IDs",
                false);
    }

    protected String getDefaultActiveHeaderStyleClass(HtmlPanelTabbedPane tabbedPane, FacesContext facesContext){
        return JavascriptUtils.getValidJavascriptName(
                "panelTabbedPane_"+tabbedPane.getClientId(facesContext)+"_ActiveStyle",
                false);
    }

    protected String getHeaderClasses(HtmlPanelTabbedPane tabbedPane, HtmlPanelTab tab, String defaultClass, String userClass){
        String headerCellClass = defaultClass;
        String userTabStyleClass = tab.getStyleClass();

        if( userClass != null )
            headerCellClass = headerCellClass + " " + userClass;

        if( userTabStyleClass != null )
            headerCellClass = headerCellClass + " " + userTabStyleClass;

        return headerCellClass;
    }

    protected void writeTableStart(ResponseWriter writer,
                                   FacesContext facesContext,
                                   HtmlPanelTabbedPane tabbedPane)
        throws IOException
    {
        String oldBgColor = tabbedPane.getBgcolor();
        tabbedPane.setBgcolor(null);

        writer.startElement(HTML.TABLE_ELEM, tabbedPane);
        writer.writeAttribute(HTML.ID_ATTR, getTableStylableId(tabbedPane,facesContext), null);
        String oldTabbedStyleClass = tabbedPane.getStyleClass();
        tabbedPane.setStyleClass ((oldTabbedStyleClass == null) ? "myFaces_panelTabbedPane" : "myFaces_panelTabbedPane " + oldTabbedStyleClass);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, "0", null);
        Map<String, List<ClientBehavior>> behaviors = tabbedPane.getClientBehaviors();
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, tabbedPane, behaviors);
            HtmlRendererUtils.renderHTMLAttributes(writer, tabbedPane, HTML.TABLE_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS); 
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, tabbedPane, HTML.TABLE_PASSTHROUGH_ATTRIBUTES);            
        }
        writer.flush();

        tabbedPane.setBgcolor(oldBgColor);
        tabbedPane.setStyleClass(oldTabbedStyleClass);
    }

    /**
     * As the colon (:) can't be used in CSS, transforms the id to make it safe to use for CSS. 
     */
    protected String getTableStylableId(HtmlPanelTabbedPane tabbedPane, FacesContext facesContext){
        String originalID = tabbedPane.getClientId( facesContext );
        return originalID.replace(':','_');
    }

    protected String getTabIndexSubmitFieldIDAndName(HtmlPanelTabbedPane tabbedPane, FacesContext facesContext){
        return tabbedPane.getClientId(facesContext)+"_indexSubmit";
    }

    protected String getHeaderCellID(HtmlPanelTab tab, FacesContext facesContext){
        return tab.getClientId(facesContext)+"_headerCell";
    }

    // Do not change without modifying the .js
    private String getSubHeaderCellID(HtmlPanelTab tab, FacesContext facesContext){
        return tab.getClientId(facesContext)+"_headerCell_sub";
    }

    protected void writeHeaderCell(ResponseWriter writer,
                                   FacesContext facesContext,
                                   HtmlPanelTabbedPane tabbedPane,
                                   HtmlPanelTab tab,
                                   int tabIndex,
                                   int visibleTabIndex,
                                   boolean active,
                                   boolean disabled)
        throws IOException
    {
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        HtmlRendererUtils.writePrettyIndent(facesContext);
        writer.startElement(HTML.TD_ELEM, tabbedPane);
        writer.writeAttribute(HTML.ID_ATTR, getHeaderCellID(tab, facesContext), null);

        if (disabled)
        {
            String disabledClass = tabbedPane.getDisabledTabStyleClass();
            writer.writeAttribute(HTML.CLASS_ATTR,
                                  DISABLED_HEADER_CELL_CLASS + (disabledClass==null ? "" : ' '+disabledClass),
                                  null);
        }
        else{
            if (active)
            {
                writer.writeAttribute(HTML.CLASS_ATTR,
                                      getHeaderClasses(tabbedPane, tab, ACTIVE_HEADER_CELL_CLASS, tabbedPane.getActiveTabStyleClass()),
                                      null);
            }
            else
            {
                writer.writeAttribute(HTML.CLASS_ATTR,
                                      getHeaderClasses(tabbedPane, tab, INACTIVE_HEADER_CELL_CLASS, tabbedPane.getInactiveTabStyleClass()),
                                      null);
            }
        }
      
        if (tab.getStyle() != null) {
            writer.writeAttribute(HTML.STYLE_ATTR, tab.getStyle(), null);
        }

        String label = tab.getLabel();
        if (label == null || label.length() == 0)
        {
            label = "Tab " + tabIndex;
        }

        if (disabled) {
            writer.startElement(HTML.LABEL_ELEM, tabbedPane);
            // writer.writeAttribute(HTML.NAME_ATTR, tabbedPane.getClientId(facesContext) + "." + tabIndex, null); // Usefull ?
            writer.writeText(label, null);
            writer.endElement(HTML.LABEL_ELEM);
        } else {
            // Button
            writer.startElement(HTML.INPUT_ELEM, tabbedPane);
            writer.writeAttribute(HTML.TYPE_ATTR, "submit", null);
            writer.writeAttribute(HTML.NAME_ATTR, tabbedPane.getClientId(facesContext) + "." + tabIndex, null);
            writer.writeAttribute(HTML.VALUE_ATTR, label, null);
            if( tabbedPane.isClientSide() ){
                String activeUserClass = tabbedPane.getActiveTabStyleClass();
                String inactiveUserClass = tabbedPane.getInactiveTabStyleClass();
                String activeSubStyleUserClass = tabbedPane.getActiveSubStyleClass();
                String inactiveSubStyleUserClass = tabbedPane.getInactiveSubStyleClass();
                
                String serverSideScript = "return myFaces_showPanelTab("
                    +tabIndex+",'"+getTabIndexSubmitFieldIDAndName(tabbedPane, facesContext)+"',"
                    +'\''+getHeaderCellID(tab, facesContext)+"','"+tab.getClientId(facesContext) + TAB_DIV_SUFFIX +"',"
                    +getHeaderCellsIDsVar(tabbedPane,facesContext)+','+getTabsIDsVar(tabbedPane,facesContext)+','
                    + (activeUserClass==null ? "null" : '\''+activeUserClass+'\'')+','+ (inactiveUserClass==null ? "null" : '\''+inactiveUserClass+'\'')+','
                    + (activeSubStyleUserClass==null ? "null" : '\''+activeSubStyleUserClass+'\'')+','+ (inactiveSubStyleUserClass==null ? "null" : '\''+inactiveSubStyleUserClass+'\'')+");";
                
                Map<String, List<ClientBehavior>> behaviors = tab.getClientBehaviors();
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, 
                        tab, ClientBehaviorEvents.CLICK, null, behaviors, HTML.ONCLICK_ATTR, 
                        (String) tab.getAttributes().get(HTML.ONCLICK_ATTR), serverSideScript);
                
                //String onclickEvent = tab.getAttributes().get(HTML.ONCLICK_ATTR) != null ? (String) tab.getAttributes().get(HTML.ONCLICK_ATTR) : "";

                //if(!("").equals(onclickEvent) && onclickEvent.charAt(onclickEvent.length()-1) !=  ';') {
                //    onclickEvent += ";";
                //}
                
                //writer.writeAttribute(HTML.ONCLICK_ATTR,
                //                      onclickEvent +
                //                      serverSideScript,
                //                      null);
            }

            writer.endElement(HTML.INPUT_ELEM);
        }
        writer.endElement(HTML.TD_ELEM);
    }


    protected void writeSubHeaderCells(ResponseWriter writer,
                                       FacesContext facesContext,
                                       HtmlPanelTabbedPane tabbedPane,
                                       int visibleTabCount,
                                       int visibleTabSelectedIndex)
            throws IOException
    {
        String activeSubStyleUserClass = tabbedPane.getActiveSubStyleClass();
        String inactiveSubStyleUserClass = tabbedPane.getInactiveSubStyleClass();

        List children = tabbedPane.getChildren();
        StringBuffer classes = new StringBuffer();
        for (int i = 0, len = children.size(), renderedIndex = 0; i < len; i++)
        {
            UIComponent child = getUIComponent((UIComponent)children.get(i));
            if (child instanceof HtmlPanelTab && child.isRendered())
            {
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);
                HtmlRendererUtils.writePrettyIndent(facesContext);
                writer.startElement(HTML.TD_ELEM, tabbedPane);
                writer.writeAttribute(HTML.ID_ATTR, getSubHeaderCellID((HtmlPanelTab)child, facesContext), null);
                classes.setLength(0);
                classes.append(SUB_HEADER_CELL_CLASS);
                if( renderedIndex == 0 ){
                    classes.append(' ');
                    classes.append(SUB_HEADER_CELL_CLASS_FIRST);
                }
                if( renderedIndex == visibleTabCount ){
                    classes.append(' ');
                    classes.append(SUB_HEADER_CELL_CLASS_LAST);
                }
                if( renderedIndex == visibleTabSelectedIndex ){
                    if( activeSubStyleUserClass != null ){
                        classes.append(' ');
                        classes.append(activeSubStyleUserClass);
                    }
                }else if( inactiveSubStyleUserClass != null ){
                    classes.append(' ');
                    classes.append(inactiveSubStyleUserClass);
                }

                classes.append(' ');
                classes.append(renderedIndex == visibleTabSelectedIndex ? SUB_HEADER_CELL_CLASS_ACTIVE : SUB_HEADER_CELL_CLASS_INACTIVE);

                writer.writeAttribute(HTML.CLASS_ATTR, classes.toString(), null);

                writer.write("&#160;");
                writer.endElement(HTML.TD_ELEM);

                renderedIndex++;
            }
        }

        // Empty Cell Sub
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        HtmlRendererUtils.writePrettyIndent(facesContext);
        writer.startElement(HTML.TD_ELEM, tabbedPane);
        writer.writeAttribute(
                HTML.CLASS_ATTR,
                SUB_HEADER_CELL_CLASS+' '+SUB_HEADER_CELL_CLASS_LAST
                +(inactiveSubStyleUserClass != null ? ' '+inactiveSubStyleUserClass : ""),
                null);
        writer.write("&#160;");
        writer.endElement(HTML.TD_ELEM);
    }

    protected void writeTabsContents(ResponseWriter writer, FacesContext facesContext, HtmlPanelTabbedPane tabbedPane,
                                     int selectedIndex) throws IOException {
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        int tabIdx = 0;
        List children = tabbedPane.getChildren();
        for (int i = 0, len = children.size(); i < len; i++) {
            UIComponent child = getUIComponent((UIComponent) children.get(i));
            if (child instanceof HtmlPanelTab) {
                String activeTabVar = tabbedPane.getActiveTabVar();
                if (activeTabVar != null) {
                    Map requestMap = facesContext.getExternalContext().getRequestMap();
                    requestMap.put(activeTabVar, Boolean.valueOf(tabIdx == selectedIndex));
                }

                HtmlPanelTab tab = (HtmlPanelTab)child;
                writer.startElement(HTML.DIV_ELEM, tabbedPane);
               writer.writeAttribute(HTML.ID_ATTR, tab.getClientId(facesContext) + TAB_DIV_SUFFIX, null);
                // the inactive tabs are hidden with a div-tag
                if (tabIdx != selectedIndex) {
                    writer.writeAttribute(HTML.STYLE_ATTR, "display:none", null);
                }
                if (tabbedPane.isClientSide() || selectedIndex == tabIdx) {
                  // render all content in client side mode or only the selected in server side mode
                  RendererUtils.renderChild(facesContext, child);
                }
                writer.endElement(HTML.DIV_ELEM);

                tabIdx++;
                if (activeTabVar != null) {
                    Map requestMap = facesContext.getExternalContext().getRequestMap();
                    requestMap.remove(tabbedPane.getActiveTabVar());
                }
            } else {
                RendererUtils.renderChild(facesContext, child);
            }
        }
   }

    private UIComponent getUIComponent(UIComponent uiComponent)
    {
        /* todo: handle forms other than UIForm */
        if (uiComponent instanceof UIForm || uiComponent instanceof UINamingContainer)
        {
            List children = uiComponent.getChildren();
            for (int i = 0, len = children.size(); i < len; i++)
            {
                uiComponent = getUIComponent((UIComponent)children.get(i));
            }
        }
        return uiComponent;
    }

    protected void writeFormEnd(ResponseWriter writer,
                                FacesContext facesContext)
        throws IOException
    {
        //write state marker
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        viewHandler.writeState(facesContext);

        writer.endElement(HTML.FORM_ELEM);
    }

    protected boolean isDisabled(FacesContext facesContext, HtmlPanelTab tab)
    {
        return !UserRoleUtils.isEnabledOnUserRole(tab) || tab.isDisabled();
    }
}
