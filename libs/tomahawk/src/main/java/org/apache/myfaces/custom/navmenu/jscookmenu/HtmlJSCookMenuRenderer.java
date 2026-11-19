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
package org.apache.myfaces.custom.navmenu.jscookmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.LibraryLocationAware;
import org.apache.myfaces.custom.navmenu.NavigationMenuItem;
import org.apache.myfaces.custom.navmenu.NavigationMenuUtils;
import org.apache.myfaces.custom.navmenu.UINavigationMenuItem;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.el.SimpleActionMethodBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlFormRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Command"
 *   type = "org.apache.myfaces.JSCookMenu"
 * 
 * @author Thomas Spiegl
 * @version $Revision: 698742 $ $Date: 2008-09-24 16:25:46 -0500 (mié, 24 sep 2008) $
 */
@ListenerFor(systemEventClass=PreRenderViewAddResourceEvent.class)
public class HtmlJSCookMenuRenderer
    extends HtmlRenderer implements ComponentSystemEventListener
{
    private static final String MYFACES_HACK_SCRIPT = "MyFacesHack.js";

    private static final String JSCOOK_MENU_SCRIPT = "JSCookMenu.js";
    
    private static final String JSCOOK_EFFECT_SCRIPT = "effect.js";    

    private static final Log log = LogFactory.getLog(HtmlJSCookMenuRenderer.class);

    private static final String JSCOOK_ACTION_PARAM = "jscook_action";
    private static final Class[] ACTION_LISTENER_ARGS = {ActionEvent.class};

    private static final Map builtInThemes = new java.util.HashMap();

    static {
        builtInThemes.put("ThemeOffice", "ThemeOffice/");
        builtInThemes.put("ThemeMiniBlack", "ThemeMiniBlack/");
        builtInThemes.put("ThemeIE", "ThemeIE/");
        builtInThemes.put("ThemePanel", "ThemePanel/");
        builtInThemes.put("ThemeGray", "ThemeGray/");        
    }

    public void processEvent(ComponentSystemEvent event)
    {
        HtmlCommandJSCookMenu menu = (HtmlCommandJSCookMenu) event.getComponent();
        String theme = menu.getTheme();
        if (theme == null) {
            // should never happen; theme is a required attribute in the jsp tag definition
            throw new IllegalArgumentException("theme name is mandatory for a jscookmenu.");
        }

        addResourcesToHeaderWithJSF2ResourceAPI(theme, menu, FacesContext.getCurrentInstance());
    }

    public void decode(FacesContext context, UIComponent component) {
        RendererUtils.checkParamValidity(context, component, HtmlCommandJSCookMenu.class);

        Map parameter = context.getExternalContext().getRequestParameterMap();
        String actionParam = (String) parameter.get(JSCOOK_ACTION_PARAM);
        if (actionParam != null && !actionParam.trim().equals("") &&
            !actionParam.trim().equals("null")) {
            String compId = getMenuId(context, component);
            StringTokenizer tokenizer = new StringTokenizer(actionParam, ":");
            if (tokenizer.countTokens() > 1) {
                String actionId = tokenizer.nextToken();
                if (! compId.equals(actionId)) {
                    return;
                }
                while (tokenizer.hasMoreTokens()) {
                    String action = tokenizer.nextToken();
                    if (action.startsWith("A]")) {
                        action = action.substring(2, action.length());
                        action = decodeValueBinding(action, context);
                        MethodBinding mb;
                        if (NavigationMenuUtils.isValueReference(action)) {
                            mb = context.getApplication().createMethodBinding(action, null);
                        }
                        else {
                            mb = new SimpleActionMethodBinding(action);
                        }
                        ((HtmlCommandJSCookMenu) component).setAction(mb);
                    }
                    else if (action.startsWith("L]")) {
                        action = action.substring(2, action.length());
                        String value = null;
                        int idx = action.indexOf(";");
                        if (idx > 0 && idx < action.length() - 1) {
                            value = action.substring(idx + 1, action.length());
                            action = action.substring(0, idx);
                            ((HtmlCommandJSCookMenu) component).setValue(value);
                        }
                        else if (idx == action.length() - 1)
                        {
                            value = null; //No Value found, so set it to null as expected
                            action = action.substring(0, idx);
                        }
                        MethodBinding mb;
                        if (NavigationMenuUtils.isValueReference(action)) {
                            mb = context.getApplication().createMethodBinding(action, ACTION_LISTENER_ARGS);
                            ((HtmlCommandJSCookMenu) component).setActionListener(mb);
                            if (value != null)
                                ((HtmlCommandJSCookMenu) component).setValue(value);
                        }
                    }
                }
            }
            component.queueEvent(new ActionEvent(component));
        }
    }

    private String decodeValueBinding(String actionParam, FacesContext context) {
        int idx = actionParam.indexOf(";#{");
        if (idx == -1) {
            return actionParam;
        }

        String newActionParam = actionParam.substring(0, idx);
        String vbParam = actionParam.substring(idx + 1);

        idx = vbParam.indexOf('=');
        if (idx == -1) {
            return newActionParam;
        }
        String vbExpressionString = vbParam.substring(0, idx);
        String vbValue = vbParam.substring(idx + 1);

        ValueBinding vb =
            context.getApplication().createValueBinding(vbExpressionString);
        vb.setValue(context, vbValue);

        return newActionParam;
    }

    public boolean getRendersChildren() {
        return true;
    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        RendererUtils.checkParamValidity(context, component, HtmlCommandJSCookMenu.class);

        List list = NavigationMenuUtils.getNavigationMenuItemList(component);
        if (list.size() > 0) {
            FormInfo parentFormInfo = RendererUtils.findNestingForm(component, context);
            ResponseWriter writer = context.getResponseWriter();

            if (parentFormInfo == null)
                throw new FacesException("jscook menu is not embedded in a form.");
            String formName = parentFormInfo.getFormName();
            List uiNavMenuItemList = component.getChildren();
            /* todo: disabled for now. Check if dummy form stuff is still needed/desired
                if( formName == null ) {
                DummyFormUtils.setWriteDummyForm(context,true);
                DummyFormUtils.addDummyFormParameter(context,JSCOOK_ACTION_PARAM);

                formName = DummyFormUtils.getDummyFormName();
            }
            else {*/
            if (RendererUtils.isAdfOrTrinidadForm(parentFormInfo.getForm())) {
                // need to add hidden input, cause MyFaces form is missing hence will not render hidden inputs
                writer.write("<input type=\"hidden\" name=\"");
                writer.write(JSCOOK_ACTION_PARAM);
                writer.write("\" />");
            }
            else {
                HtmlFormRendererBase.addHiddenCommandParameter(context, parentFormInfo.getForm(), JSCOOK_ACTION_PARAM);
            }

            //}

            String myId = getMenuId(context, component);

            writer.startElement(HTML.SCRIPT_ELEM, component);
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
            StringBuffer script = new StringBuffer();
            script.append("var ").append(getMenuId(context, component)).append(" =\n[");
            encodeNavigationMenuItems(context, script,
                                      (NavigationMenuItem[]) list.toArray(new NavigationMenuItem[list.size()]),
                                      uiNavMenuItemList,
                                      myId, formName);

            script.append("];");
            writer.writeText(script.toString(), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }

    private void encodeNavigationMenuItems(FacesContext context,
                                           StringBuffer writer,
                                           NavigationMenuItem[] items,
                                           List uiNavMenuItemList,
                                           String menuId, String formName)
        throws IOException {
        for (int i = 0; i < items.length; i++) {
            NavigationMenuItem item = items[i];
            Object tempObj = null;
            UINavigationMenuItem uiNavMenuItem = null;
            if (i < uiNavMenuItemList.size()) {
                tempObj = uiNavMenuItemList.get(i);
            }
            if (tempObj != null) {
                if (tempObj instanceof UINavigationMenuItem) {
                    uiNavMenuItem = (UINavigationMenuItem) tempObj;
                }
            }

            if (! item.isRendered()) {
                continue;
            }

            if (i > 0) {
                writer.append(",\n");
            }

            if (item.isSplit()) {
                writer.append("_cmSplit,");

                if (item.getLabel().equals("0")) {
                    continue;
                }
            }

            writer.append("[");
            if (item.getIcon() != null) {
                String iconSrc = context.getApplication().getViewHandler().getResourceURL(context, item.getIcon());
                writer.append("'<img src=\"");
                writer.append(context.getExternalContext().encodeResourceURL(iconSrc));
                writer.append("\"/>'");
            }
            else {
                writer.append("null");
            }
            writer.append(", '");
            if (item.getLabel() != null) {
                writer.append(getString(context, item.getLabel()));
            }
            writer.append("', ");
            StringBuffer actionStr = new StringBuffer();
            if ((item.getAction() != null || item.getActionListener() != null) && ! item.isDisabled()) {
                actionStr.append("'");
                actionStr.append(menuId);
                if (item.getActionListener() != null) {
                    actionStr.append(":L]");
                    actionStr.append(item.getActionListener());
                    if (uiNavMenuItem != null && uiNavMenuItem.getItemValue() != null) {
                        actionStr.append(';');
                        actionStr.append(getString(context, uiNavMenuItem.getItemValue()));
                    }
                    else if (item.getValue() != null) {
                        actionStr.append(';');
                        actionStr.append(getString(context, item.getValue()));
                    }
                }
                if (item.getAction() != null) {
                    actionStr.append(":A]");
                    actionStr.append(item.getAction());
                    if (uiNavMenuItem != null) {
                        encodeValueBinding(actionStr, uiNavMenuItem, item);
                    }
                }
                actionStr.append("'");
                writer.append(actionStr.toString());
            }
            else {
                writer.append("null");
            }
            writer.append(", '");
            // Change here to allow the use of non dummy form.
            writer.append(formName);
            writer.append("', null");

            if (item.isRendered() && ! item.isDisabled()) {
                // render children only if parent is visible/enabled
                NavigationMenuItem[] menuItems = item.getNavigationMenuItems();
                if (menuItems != null && menuItems.length > 0) {
                    writer.append(",");
                    if (uiNavMenuItem != null) {
                        encodeNavigationMenuItems(context, writer, menuItems,
                                                  uiNavMenuItem.getChildren(), menuId, formName);
                    }
                    else {
                        encodeNavigationMenuItems(context, writer, menuItems,
                                                  new ArrayList(1), menuId, formName);
                    }
                }
            }
            writer.append("]");
        }
    }

    private String getString(FacesContext facesContext, Object value) {
        String str = "";

        if (value != null) {
            str = value.toString();
        }

        if (NavigationMenuUtils.isValueReference(str)) {
            value = facesContext.getApplication().createValueBinding(str).getValue(facesContext);

            if (value != null) {
                str = value.toString();
            }
            else {
                str = "";
            }
        }

        return JavascriptUtils.encodeString(str);
    }

    private void encodeValueBinding(StringBuffer writer, UINavigationMenuItem uiNavMenuItem,
                                    NavigationMenuItem item) {
        ValueBinding vb = uiNavMenuItem.getValueBinding("NavMenuItemValue");
        if (vb == null) {
            return;
        }
        String vbExpression = vb.getExpressionString();
        if (vbExpression == null) {
            return;
        }
        Object tempObj = item.getValue();
        if (tempObj == null) {
            return;
        }

        writer.append(";");
        writer.append(vbExpression);
        writer.append("=");
        writer.append(tempObj.toString());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        HtmlCommandJSCookMenu menu = (HtmlCommandJSCookMenu) component;
        String theme = menu.getTheme();
        if (theme == null) {
            // should never happen; theme is a required attribute in the jsp tag definition
            throw new IllegalArgumentException("theme name is mandatory for a jscookmenu.");
        }

        addResourcesToHeader(theme, menu, context);

    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        RendererUtils.checkParamValidity(context, component, HtmlCommandJSCookMenu.class);
        HtmlCommandJSCookMenu menu = (HtmlCommandJSCookMenu) component;
        String theme = menu.getTheme();


        ResponseWriter writer = context.getResponseWriter();

        String menuId = getMenuId(context, component);

        writer.write("<div id=\"");
        writer.write(menuId);
        writer.write("\"></div>\n");
        writer.startElement(HTML.SCRIPT_ELEM, menu);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);

        StringBuffer buf = new StringBuffer();
        buf.append("\tif(window.cmDraw!=undefined) { cmDraw ('").
            append(menuId).
            append("', ").
            append(menuId).
            append(", '").
            append(menu.getLayout()).
            append("', cm").
            append(theme).
            append(", '").
            append(theme).
            append("');}");

        writer.writeText(buf.toString(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }

    private void addResourcesToHeaderWithJSF2ResourceAPI(String themeName, HtmlCommandJSCookMenu menu, FacesContext context) {
        String javascriptLocation = (String) menu.getAttributes().get(JSFAttr.JAVASCRIPT_LOCATION);
        String imageLocation = (String) menu.getAttributes().get(JSFAttr.IMAGE_LOCATION);
        String styleLocation = (String) menu.getAttributes().get(JSFAttr.STYLE_LOCATION);
        String javascriptLibrary = (String) menu.getAttributes().get(LibraryLocationAware.JAVASCRIPT_LIBRARY_ATTR);
        String imageLibrary = (String) menu.getAttributes().get(LibraryLocationAware.IMAGE_LIBRARY_ATTR);
        String styleLibrary = (String) menu.getAttributes().get(LibraryLocationAware.STYLE_LIBRARY_ATTR);

        //AddResource addResource = AddResourceFactory.getInstance(context);

        //if (javascriptLocation != null) {
        //    addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + JSCOOK_MENU_SCRIPT);
        //    addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + JSCOOK_EFFECT_SCRIPT);            
        //    addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + MYFACES_HACK_SCRIPT);
        //}
        //else 
        if (javascriptLocation == null)
        {
            if (javascriptLibrary == null)
            {
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlJSCookMenuRenderer.class, JSCOOK_MENU_SCRIPT);
                TomahawkResourceUtils.addOutputScriptResource(context, "oam.custom.navmenu.jscookmenu", JSCOOK_MENU_SCRIPT);
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlJSCookMenuRenderer.class, JSCOOK_EFFECT_SCRIPT);
                TomahawkResourceUtils.addOutputScriptResource(context, "oam.custom.navmenu.jscookmenu", JSCOOK_EFFECT_SCRIPT);
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlJSCookMenuRenderer.class, MYFACES_HACK_SCRIPT);
                TomahawkResourceUtils.addOutputScriptResource(context, "oam.custom.navmenu.jscookmenu", MYFACES_HACK_SCRIPT);
            }
            else
            {
                TomahawkResourceUtils.addOutputScriptResource(context, javascriptLibrary, JSCOOK_MENU_SCRIPT);
                TomahawkResourceUtils.addOutputScriptResource(context, javascriptLibrary, JSCOOK_EFFECT_SCRIPT);
                TomahawkResourceUtils.addOutputScriptResource(context, javascriptLibrary, MYFACES_HACK_SCRIPT);
            }
        }

        addThemeSpecificResourcesWithJSF2ResourceAPI(themeName, styleLocation, javascriptLocation, imageLocation, 
                styleLibrary, javascriptLibrary, imageLibrary, context);
    }
    
    private void addResourcesToHeader(String themeName, HtmlCommandJSCookMenu menu, FacesContext context) {
        String javascriptLocation = (String) menu.getAttributes().get(JSFAttr.JAVASCRIPT_LOCATION);
        String imageLocation = (String) menu.getAttributes().get(JSFAttr.IMAGE_LOCATION);
        String styleLocation = (String) menu.getAttributes().get(JSFAttr.STYLE_LOCATION);
        String javascriptLibrary = (String) menu.getAttributes().get(LibraryLocationAware.JAVASCRIPT_LIBRARY_ATTR);
        String imageLibrary = (String) menu.getAttributes().get(LibraryLocationAware.IMAGE_LIBRARY_ATTR);
        String styleLibrary = (String) menu.getAttributes().get(LibraryLocationAware.STYLE_LIBRARY_ATTR);

        AddResource addResource = AddResourceFactory.getInstance(context);

        if (javascriptLocation != null) {
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + JSCOOK_MENU_SCRIPT);
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + JSCOOK_EFFECT_SCRIPT);            
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + MYFACES_HACK_SCRIPT);
        }

        addThemeSpecificResources(themeName, styleLocation, javascriptLocation, imageLocation, 
                styleLibrary, javascriptLibrary, imageLibrary, context);
    }

    /**
     * A theme for a menu requires a number of external files; this method
     * outputs those into the page head section.
     *
     * @param themeName          is the name of the theme for this menu. It is never
     *                           null. It may match one of the built-in theme names or may be a custom
     *                           theme defined by the application.
     * @param styleLocation      is the URL of a directory containing a
     *                           "theme.css" file. A stylesheet link tag will be inserted into
     *                           the page header referencing that file. If null then if the
     *                           themeName is a built-in one then a reference to the appropriate
     *                           built-in stylesheet is generated (requires the ExtensionsFilter).
     *                           If null and a custom theme is used then no stylesheet link will be
     *                           generated here.
     * @param javascriptLocation is the URL of a directory containing a
     *                           "theme.js" file. A script tag will be inserted into the page header
     *                           referencing that file. If null then if the themeName is a built-in
     *                           one then a reference to the built-in stylesheet is generated (requires
     *                           the ExtensionsFilter). If null and a custom theme is used then no
     *                           stylesheet link will be generated here.
     * @param imageLocation      is the URL of a directory containing files
     *                           (esp. image files) used by the theme.js file to define the menu
     *                           theme. A javascript variable of name "my{themeName}Base" is
     *                           generated in the page header containing this URL, so that the
     *                           theme.js script can locate the files. If null then if the themeName
     *                           is a built-in one then the URL to the appropriate resource directory
     *                           is generated (requires the ExtensionsFilter). If null and a custom
     *                           theme is used then no javascript variable will be generated here.
     * @param context            is the current faces context.
     */
    private void addThemeSpecificResourcesWithJSF2ResourceAPI(String themeName, String styleLocation,
                                           String javascriptLocation, String imageLocation,
                                           String styleLibrary, String javascriptLibrary,
                                           String imageLibrary, FacesContext context) {
        String themeLocation = (String) builtInThemes.get(themeName);
        if (themeLocation == null) {
            log.debug("Unknown theme name '" + themeName + "' specified.");
        }

        //AddResource addResource = AddResourceFactory.getInstance(context);

        if ((imageLocation != null) || (themeLocation != null)) {
            // Generate a javascript variable containing a reference to the
            // directory containing theme image files, for use by the theme
            // javascript file. If neither of these is defined (ie a custom
            // theme was specified but no imageLocation) then presumably the
            // theme.js file uses some other mechanism to determine where
            // its image files are.
            StringBuffer buf = new StringBuffer();
            buf.append("var my");
            buf.append(themeName);
            buf.append("Base='");
            ExternalContext externalContext = context.getExternalContext();
            //if (imageLocation != null) {
            //    buf.append(externalContext.encodeResourceURL(addResource.getResourceUri(context,
            //                                                                            imageLocation + "/" + themeName)));
            //    buf.append("';");
            //    addResource.addInlineScriptAtPosition(context, AddResource.HEADER_BEGIN, buf.toString());
            //}
            //else 
            if (imageLocation == null)
            {
                //buf.append(externalContext.encodeResourceURL(addResource.getResourceUri(context,
                //        HtmlJSCookMenuRenderer.class, themeLocation)));
                Resource resource = context.getApplication().getResourceHandler().
                    createResource(";j","oam.custom.navmenu.jscookmenu."+themeName);
                buf.append(resource.getRequestPath());
                buf.append("';");
                TomahawkResourceUtils.addInlineOutputScriptResource(context, TomahawkResourceUtils.HEAD_LOCATION, buf.toString());
            }
        }
        else
        {
            if ((imageLibrary != null))
            {
                StringBuffer buf = new StringBuffer();
                buf.append("var my");
                buf.append(themeName);
                buf.append("Base='");
                Resource resource = context.getApplication().getResourceHandler().
                    createResource(";j",imageLibrary+'.'+themeName);
                buf.append(resource.getRequestPath());
                buf.append("';");
                //addResource.addInlineScriptAtPosition(context, AddResource.HEADER_BEGIN, buf.toString());
                TomahawkResourceUtils.addInlineOutputScriptResource(context, TomahawkResourceUtils.HEAD_LOCATION, buf.toString());
            }
        }

        if ((javascriptLocation != null) || (themeLocation != null)) {
            // Generate a <script> tag in the page header pointing to the
            // theme.js file for this theme. If neither of these is defined
            // then presumably the theme.js file is referenced by a <script>
            // tag hard-wired into the page or inserted via some other means.
            //if (javascriptLocation != null) {
                // For now, assume that if the user specified a location for a custom
                // version of the jscookMenu.js file then the theme.js file can be found
                // in the same location.
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/" + themeName
                //    + "/theme.js");
            //}
            //else
            if (javascriptLocation == null)
            {
                // Using a built-in theme, so we know where the theme.js file is.
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlJSCookMenuRenderer.class, themeName
                //    + "/theme.js");
                TomahawkResourceUtils.addOutputScriptResource(context, "oam.custom.navmenu.jscookmenu."+themeName, "theme.js");
            }
        }
        else
        {
            if ((javascriptLibrary != null))
            {
                TomahawkResourceUtils.addOutputScriptResource(context, javascriptLibrary+'.'+themeName, "theme.js");
            }
        }

        if ((styleLocation != null) || (themeLocation != null)) {
            // Generate a <link type="text/css"> tag in the page header pointing to
            // the theme stylesheet. If neither of these is defined then presumably
            // the stylesheet is referenced by a <link> tag hard-wired into the page
            // or inserted via some other means.
            //if (styleLocation != null) {
            //    addResource.addStyleSheet(context, AddResource.HEADER_BEGIN, styleLocation + "/" + themeName + "/theme.css");
            //}
            //else
            if (styleLocation == null)
            {
                //addResource.addStyleSheet(context, AddResource.HEADER_BEGIN, HtmlJSCookMenuRenderer.class, themeName
                //    + "/theme.css");
                TomahawkResourceUtils.addOutputStylesheetResource(context, "oam.custom.navmenu.jscookmenu."+themeName, "theme.css");
            }
        }
        else
        {
            if ((styleLibrary != null))
            {
                TomahawkResourceUtils.addOutputStylesheetResource(context, styleLibrary+'.'+themeName, "theme.css");
            }
        }
    }
    
    private void addThemeSpecificResources(String themeName,
            String styleLocation, String javascriptLocation,
            String imageLocation, String styleLibrary,
            String javascriptLibrary, String imageLibrary, FacesContext context)
    {
        String themeLocation = (String) builtInThemes.get(themeName);
        if (themeLocation == null)
        {
            log.debug("Unknown theme name '" + themeName + "' specified.");
        }

        AddResource addResource = AddResourceFactory.getInstance(context);

        if ((imageLocation != null) || (themeLocation != null))
        {
            // Generate a javascript variable containing a reference to the
            // directory containing theme image files, for use by the theme
            // javascript file. If neither of these is defined (ie a custom
            // theme was specified but no imageLocation) then presumably the
            // theme.js file uses some other mechanism to determine where
            // its image files are.
            StringBuffer buf = new StringBuffer();
            buf.append("var my");
            buf.append(themeName);
            buf.append("Base='");
            ExternalContext externalContext = context.getExternalContext();
            if (imageLocation != null)
            {
                buf.append(externalContext.encodeResourceURL(addResource
                        .getResourceUri(context, imageLocation + "/"
                                + themeName)));
                buf.append("';");
                addResource.addInlineScriptAtPosition(context,
                        AddResource.HEADER_BEGIN, buf.toString());
            }
        }

        if ((javascriptLocation != null) || (themeLocation != null))
        {
            // Generate a <script> tag in the page header pointing to the
            // theme.js file for this theme. If neither of these is defined
            // then presumably the theme.js file is referenced by a <script>
            // tag hard-wired into the page or inserted via some other means.
            if (javascriptLocation != null)
            {
                // For now, assume that if the user specified a location for a custom
                // version of the jscookMenu.js file then the theme.js file can be found
                // in the same location.
                addResource.addJavaScriptAtPosition(context,
                        AddResource.HEADER_BEGIN, javascriptLocation + "/"
                                + themeName + "/theme.js");
            }
        }

        if ((styleLocation != null) || (themeLocation != null))
        {
            // Generate a <link type="text/css"> tag in the page header pointing to
            // the theme stylesheet. If neither of these is defined then presumably
            // the stylesheet is referenced by a <link> tag hard-wired into the page
            // or inserted via some other means.
            if (styleLocation != null)
            {
                addResource.addStyleSheet(context, AddResource.HEADER_BEGIN,
                        styleLocation + "/" + themeName + "/theme.css");
            }
        }
    }

    /**
     * Fetch the very last part of the menu id.
     *
     * @param context
     * @param component
     * @return String id of the menu
     */
    private String getMenuId(FacesContext context, UIComponent component) {
        String menuId = component.getClientId(context).replaceAll(":", "_") + "_menu";
        while (menuId.startsWith("_")) {
            menuId = menuId.substring(1);
        }
        return menuId;
    }
}
