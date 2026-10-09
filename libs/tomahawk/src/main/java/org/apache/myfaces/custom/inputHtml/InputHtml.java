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

import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ListenerFor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.component.html.ext.HtmlInputText;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * HTML Editor using the kupu library.
 * http://kupu.oscom.org/
 *
 * An inline HTML based word processor based on the Kupu library. 
 * 
 * See http://kupu.oscom.org 
 * 
 * Right now, the support is limited to one editor per page 
 * (but you can use tabs to have multiple editors, but only 
 * one rendered at a time). 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 *
 * @author Sylvain Vieujot (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (jue, 03 jul 2008) $
 */
@JSFComponent(
   name = "t:inputHtml",
   tagClass = "org.apache.myfaces.custom.inputHtml.InputHtmlTag",
   composite=true)
@ListenerFor(systemEventClass=PreRenderViewAddResourceEvent.class)
public class InputHtml extends HtmlInputText implements NamingContainer, UniqueIdVendor {
    public static final String COMPONENT_TYPE = "org.apache.myfaces.InputHtml";

    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.InputHtml";

    private static final String INPUT_HTML_LIBRARY = "oam.custom.inputHtml";
    private static final String INPUT_HTML_LIBRARY_KUPU_DRAWERS = "oam.custom.inputHtml.kupudrawers";

    private static final Log log = LogFactory.getLog(HtmlInputText.class);

    public InputHtml() {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }
    
    @Override
    public void setRendererType(String rendererType)
    {
        if (!"jakarta.faces.Composite".equals(rendererType))
        {
            super.setRendererType(rendererType);
        }
    }
    
    public void processEvent(ComponentSystemEvent event)
    {
        super.processEvent(event);
        if (event instanceof PreRenderViewAddResourceEvent)
        {
            InputHtml editor = (InputHtml) event.getComponent();
            if( !HtmlRendererUtils.isDisplayValueOnly(editor) && !useFallback(editor))
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                //AddResource addResource = AddResourceFactory.getInstance(facesContext);
                //addResource.addStyleSheet(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupustyles.css");
                TomahawkResourceUtils.addOutputStylesheetResource(facesContext, INPUT_HTML_LIBRARY, "myFacesKupustyles.css");
                //addResource.addStyleSheet(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupudrawerstyles.css");
                TomahawkResourceUtils.addOutputStylesheetResource(facesContext, INPUT_HTML_LIBRARY, "kupudrawerstyles.css");
                //addResource.addStyleSheet(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "myFaces.css");
                TomahawkResourceUtils.addOutputStylesheetResource(facesContext, INPUT_HTML_LIBRARY, "myFaces.css");
    
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "sarissa.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "sarissa.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "sarissa_ieemu_xpath.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "sarissa_ieemu_xpath.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupuhelpers.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupuhelpers.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupueditor.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupueditor.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupubasetools.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupubasetools.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupuloggers.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupuloggers.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupunoi18n.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupunoi18n.js");
                //addResource.addJavaScriptAtPosition(context, InputHtmlRenderer.class, "i18n/i18n.js"); //NO
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupucleanupexpressions.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupucleanupexpressions.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupucontentfilters.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupucontentfilters.js");
    
                if (editor.isShowAnyToolBox())
                {
                    //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kuputoolcollapser.js");
                    TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kuputoolcollapser.js");
                }
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupucontextmenu.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupucontextmenu.js");
    
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupuinit.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupuinit.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupustart.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupustart.js");
    
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupusourceedit.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupusourceedit.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupuspellchecker.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupuspellchecker.js");
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupudrawers.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupudrawers.js");
    
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "kupuundo.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "kupuundo.js");
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "diff_match_patch.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "diff_match_patch.js");
    
                //addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, InputHtmlRenderer.class, "myFacesUtils.js");
                TomahawkResourceUtils.addOutputScriptResource(facesContext, INPUT_HTML_LIBRARY, "myFacesUtils.js");
                
                if( editor.getStyle()!=null ){
                    // Convert the style into an style declaration so that it doesn't preempt the Zoom works as it's relying on changing the class
                    //addResource.addInlineStyleAtPosition(
                    //        context, AddResource.HEADER_BEGIN,
                    //        "#kupu-editor{height: inherit;}\n"+
                    //        "div.kupu-fulleditor{"+editor.getStyle()+"}");
                    TomahawkResourceUtils.addInlineOutputStylesheetResource(facesContext, 
                            "#kupu-editor{height: inherit;}\n"+
                            "div.kupu-fulleditor{"+editor.getStyle()+"}");
                }
            }
        }
    }
    
    private static boolean useFallback(InputHtml editor){
        // TODO : Handle fallback="auto"
        return editor.getFallback().equals("true");
    }
    /*
    <xml id="kupuconfig" class="kupuconfig">
      <kupuconfig>
        
  <dst>#{resource['oam.custom.inputHtml:fulldoc.html']}</dst>
  <use_css>1</use_css>
  <reload_after_save>0</reload_after_save>
  <strict_output>1</strict_output>
  <content_type>application/xhtml+xml</content_type>
  <compatible_singletons>1</compatible_singletons>
  <table_classes>
    <class>plain</class>
    <class>listing</class>
    <class>grid</class>
    <class>data</class>
  </table_classes>

  <cleanup_expressions>
    <set>
      <name>Convert single quotes to curly ones</name>
      <expression>
        <reg>
          (\W)'
        </reg>
        <replacement>
          \1&#8216;
        </replacement>
      </expression>
      <expression>
        <reg>
          '
        </reg>
        <replacement>
          &#8217;
        </replacement>
      </expression>
    </set>
    <set>
      <name>Reduce whitespace</name>
      <expression>
        <reg>
          [\n\r\t]
        </reg>
        <replacement>
          \x20
        </replacement>
      </expression>
      <expression>
        <reg>
          [ ]{2}
        </reg>
        <replacement>
          \x20
        </replacement>
      </expression>
    </set>
  </cleanup_expressions>

  <image_xsl_uri>#{resource['oam.custom.inputHtml.kupudrawers:drawer.xsl']}</image_xsl_uri>
  <link_xsl_uri>#{resource['oam.custom.inputHtml.kupudrawers:drawer.xsl']}</link_xsl_uri>
  <image_libraries_uri>#{resource['oam.custom.inputHtml.kupudrawers:imagelibrary.xml']}</image_libraries_uri>
  <link_libraries_uri>#{resource['oam.custom.inputHtml.kupudrawers:linklibrary.xml']}</link_libraries_uri>
  <search_images_uri> </search_images_uri>
  <search_links_uri> </search_links_uri>

      </kupuconfig>
    </xml>
   */
    public String getKupuConfig()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<xml id=\"kupuconfig\" class=\"kupuconfig\">");
        builder.append("<kupuconfig>");
        builder.append("<dst>fulldoc.html</dst>");
        builder.append("<use_css>1</use_css>");
        builder.append("<reload_after_save>0</reload_after_save>");
        builder.append("<strict_output>1</strict_output>");
        builder.append("<content_type>application/xhtml+xml</content_type>");
        builder.append("<compatible_singletons>1</compatible_singletons>");
        builder.append("<table_classes>");
        builder.append("<class>plain</class>");
        builder.append("<class>listing</class>");
        builder.append("<class>grid</class>");
        builder.append("<class>data</class>");
        builder.append("</table_classes>");

        builder.append("<cleanup_expressions>");
        builder.append("<set>");
        builder.append("<name>Convert single quotes to curly ones</name>");
        builder.append("<expression>");
        builder.append("<reg>");
        builder.append("(\\W)'");
        builder.append("</reg>");
        builder.append("<replacement>");
        builder.append("\1&#8216;");
        builder.append("</replacement>");
        builder.append("</expression>");
        builder.append("<expression>");
        builder.append("<reg>");
        builder.append("'");
        builder.append("</reg>");
        builder.append("<replacement>");
        builder.append("&#8217;");
        builder.append("</replacement>");
        builder.append("</expression>");
        builder.append("</set>");
        builder.append("<set>");
        builder.append("<name>Reduce whitespace</name>");
        builder.append("<expression>");
        builder.append("<reg>");
        builder.append("[\n\r\t]");
        builder.append("</reg>");
        builder.append("<replacement>");
        builder.append("\\x20");
        builder.append("</replacement>");
        builder.append("</expression>");
        builder.append("<expression>");
        builder.append("<reg>");
        builder.append("[ ]{2}");
        builder.append("</reg>");
        builder.append("<replacement>");
        builder.append("\\x20");
        builder.append("</replacement>");
        builder.append("</expression>");
        builder.append("</set>");
        builder.append("</cleanup_expressions>");

        FacesContext context = getFacesContext();
        Resource resource = context.getApplication().getResourceHandler()
                .createResource("drawer.xsl",
                        INPUT_HTML_LIBRARY_KUPU_DRAWERS);
        builder.append("<image_xsl_uri>" + resource.getRequestPath()
                + "</image_xsl_uri>");
        builder.append("<link_xsl_uri>" + resource.getRequestPath()
                + "</link_xsl_uri>");

        resource = context.getApplication().getResourceHandler()
                .createResource("imagelibrary.xml",
                        INPUT_HTML_LIBRARY_KUPU_DRAWERS);
        builder.append("<image_libraries_uri>" + resource.getRequestPath()
                + "</image_libraries_uri>");

        resource = context.getApplication().getResourceHandler()
                .createResource("linklibrary.xml",
                        INPUT_HTML_LIBRARY_KUPU_DRAWERS);
        builder.append("<link_libraries_uri>" + resource.getRequestPath()
                + "</link_libraries_uri>");

        builder.append("<search_images_uri> </search_images_uri>");
        builder.append("<search_links_uri> </search_links_uri>");

        builder.append("</kupuconfig>");
        builder.append("</xml>");
        return builder.toString();
    }
    
    
    public String getDisplayValueOnlyText()
    {
        return getHtmlBody(RendererUtils.getStringValue(getFacesContext(), this));
    }
    
    public String getFallbackText()
    {
        String text = RendererUtils.getStringValue(getFacesContext(), this);
        return htmlToPlainText(text, this);
    }
    
    public String getHiddenText()
    {
        return RendererUtils.getStringValue(getFacesContext(), this);
    }
    
    private static String htmlToPlainText(String html, InputHtml editor){
        return editor.getHtmlBody( html )
                .replaceAll("<br.*>","\n")
                .replaceAll("<.+?>", "");
    }
    
    
    public String getFormId()
    {
        FormInfo parentFormInfo = RendererUtils.findNestingForm(this, getFacesContext());
        if(parentFormInfo == null)
            throw new FacesException("InputHtml must be embedded in a form.");
        return parentFormInfo.getFormName(); 
    }
    
    public String getEncodedText()
    {
        String text = this.getValueAsHtmlDocument(getFacesContext());
        return (text == null) ? "" : JavascriptUtils.encodeString( text );

    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @since 2.0
     */
    public String createUniqueId(FacesContext context, String seed)
    {
        StringBuilder bld = new StringBuilder();

        // Generate an identifier for a component. The identifier will be prefixed with UNIQUE_ID_PREFIX, and will be unique within this UIViewRoot. 
        if(seed==null)
        {
            Long uniqueIdCounter = (Long) getStateHelper().get(PropertyKeys.uniqueIdCounter);
            uniqueIdCounter = (uniqueIdCounter == null) ? 0 : uniqueIdCounter;
            getStateHelper().put(PropertyKeys.uniqueIdCounter, (uniqueIdCounter+1L));
            return bld.append(UIViewRoot.UNIQUE_ID_PREFIX).append(uniqueIdCounter).toString();    
        }
        // Optionally, a unique seed value can be supplied by component creators which should be included in the generated unique id.
        else
        {
            return bld.append(UIViewRoot.UNIQUE_ID_PREFIX).append(seed).toString();
        }
    }    
    
    /**
     * Use a text area instead of the javascript HTML editor. 
     * 
     * Default is false. Use with caution.
     * 
     * @JSFProperty
     */
    public String getFallback(){
        return (String) getStateHelper().eval(PropertyKeys.fallback, "false");
    }
    public void setFallback(String _fallback){
        getStateHelper().put(PropertyKeys.fallback, _fallback);
    }

    /**
     * The type of the value. It can be either fragment for an HTML 
     * fragment (default) or document for a full HTML document, with 
     * head, title, body, ... tags.
     * 
     * @JSFProperty
     */
    public String getType(){
        return (String) getStateHelper().eval(PropertyKeys.type, "fragment");
    }
    public void setType(String _type){
        getStateHelper().put(PropertyKeys.type, _type);
    }
    public boolean isTypeDocument(){
        return getType().equals("document");
    }

    /**
     * Allows the user to edit the HTML source code. Default is true.
     * 
     * @JSFProperty
     */
    public boolean isAllowEditSource(){
        return (Boolean) getStateHelper().eval(PropertyKeys.allowEditSource, Boolean.TRUE);
    }
    public void setAllowEditSource(boolean allowEditSource){
        getStateHelper().put(PropertyKeys.allowEditSource, allowEditSource);
    }

    /**
     * Allows the user to insert external links. Default is true.
     * 
     * @JSFProperty
     */
    public boolean isAllowExternalLinks(){
        return (Boolean) getStateHelper().eval(PropertyKeys.allowExternalLinks, Boolean.TRUE);
    }
    public void setAllowExternalLinks(boolean allowExternalLinks){
        getStateHelper().put(PropertyKeys.allowExternalLinks, allowExternalLinks);
    }

    /**
     * Show the Kupu Logo in the buttons bar. Default is true.
     * 
     * @JSFProperty
     */
    public boolean isAddKupuLogo(){
        return (Boolean) getStateHelper().eval(PropertyKeys.addKupuLogo, Boolean.TRUE);
    }
    public void setAddKupuLogo(boolean addKupuLogo){
        getStateHelper().put(PropertyKeys.addKupuLogo, addKupuLogo);
    }

    /**
     * Shortcut to avoid setting all the showXXToolBox to true. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowAllToolBoxes(){
        return (Boolean) getStateHelper().eval(PropertyKeys.showAllToolBoxes, Boolean.FALSE);
    }
    public void setShowAllToolBoxes(boolean showAllToolBoxes){
        getStateHelper().put(PropertyKeys.showAllToolBoxes, showAllToolBoxes);
    }

    /**
     * Show the Properties tool box next to the text. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowPropertiesToolBox(){
        if( isShowAllToolBoxes() )
            return true;

        return (Boolean) getStateHelper().eval(PropertyKeys.showPropertiesToolBox, Boolean.FALSE);
    }

    public void setShowPropertiesToolBox(boolean showPropertiesToolBox){
        getStateHelper().put(PropertyKeys.showPropertiesToolBox, showPropertiesToolBox);
    }

    /**
     * Show the Links tool box next to the text. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowLinksToolBox(){
        if( isShowAllToolBoxes() )
            return true;

        return (Boolean) getStateHelper().eval(PropertyKeys.showLinksToolBox, Boolean.FALSE);
    }
    
    public void setShowLinksToolBox(boolean showLinksToolBox){
        getStateHelper().put(PropertyKeys.showLinksToolBox, showLinksToolBox);
    }

    /**
     * Show the Images tool box next to the text. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowImagesToolBox(){
        if( isShowAllToolBoxes() )
            return true;

        return (Boolean) getStateHelper().eval(PropertyKeys.showImagesToolBox, Boolean.FALSE);
    }
    public void setShowImagesToolBox(boolean showImagesToolBox){
        getStateHelper().put(PropertyKeys.showImagesToolBox, showImagesToolBox);
    }

    /**
     * Show the Tables tool box next to the text. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowTablesToolBox(){
        if( isShowAllToolBoxes() )
            return true;

        return (Boolean) getStateHelper().eval(PropertyKeys.showTablesToolBox, Boolean.FALSE);
    }
    public void setShowTablesToolBox(boolean showTablesToolBox){
        getStateHelper().put(PropertyKeys.showTablesToolBox, showTablesToolBox);
    }

    /**
     * Show the Cleanup Expressions tool box next to the text. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowCleanupExpressionsToolBox(){
        if( isShowAllToolBoxes() )
            return true;

        return (Boolean) getStateHelper().eval(PropertyKeys.showCleanupExpressionsToolBox, Boolean.FALSE);
    }
    public void setShowCleanupExpressionsToolBox(boolean showCleanupExpressionsToolBox){
        getStateHelper().put(PropertyKeys.showCleanupExpressionsToolBox, showCleanupExpressionsToolBox);
    }

    /**
     * Show the Debug tool box next to the text. Default is false.
     * 
     * @JSFProperty
     */
    public boolean isShowDebugToolBox(){
        if( isShowAllToolBoxes() )
            return true;

        return (Boolean) getStateHelper().eval(PropertyKeys.showDebugToolBox, Boolean.FALSE);
    }
    public void setShowDebugToolBox(boolean showTablesToolBox){        
        getStateHelper().put(PropertyKeys.showDebugToolBox, showTablesToolBox);
    }

    public boolean isShowAnyToolBox(){
           return isShowAllToolBoxes()
               || isShowPropertiesToolBox()
               || isShowLinksToolBox()
               || isShowImagesToolBox()
               || isShowTablesToolBox()
               || isShowCleanupExpressionsToolBox()
               || isShowDebugToolBox();
    }

    public String getValueAsHtmlDocument(FacesContext context){
        String val = RendererUtils.getStringValue(context, this);
        if( isHtmlDocument( val ) )
            return val;

        return "<html><body>"+(val==null ? "" : val)+"</body></html>";
    }

    private static boolean isHtmlDocument(String text){
        if( text == null )
            return false;

        if( text.indexOf("<body>")!=-1 || text.indexOf("<body ")!=-1
            || text.indexOf("<BODY>")!=-1 || text.indexOf("<BODY ")!=-1 )
            return true;

        return false;
    }

    public String getValueFromDocument(String text){
        if( text == null )
            return "";

        if( isTypeDocument() )
            return text.trim();

        if( !isHtmlDocument(text) )
            return text.trim();

        // Extract the fragment from the document.
        String fragment = getHtmlBody( text );
        if( fragment.endsWith("<br />") )
            return fragment.substring(0, fragment.length()-6);
        return fragment;
    }
    
    String getHtmlBody(String html){
        html = html.trim();
        if( html.length() == 0 )
            return "";

        String lcText = html.toLowerCase();
        int textLength = lcText.length();
        int bodyStartIndex = -1;
        while(bodyStartIndex < textLength){
            bodyStartIndex++;
            bodyStartIndex = lcText.indexOf("<body", bodyStartIndex);
            if( bodyStartIndex == -1 )
                break; // not found.

            bodyStartIndex += 5;
            char c = lcText.charAt(bodyStartIndex);
            if( c=='>' )
                break;

            if( c!=' ' && c!='\t' )
                continue;

            bodyStartIndex = lcText.indexOf('>', bodyStartIndex);
            break;
        }
        bodyStartIndex++;

        int bodyEndIndex = lcText.lastIndexOf("</body>")-1;
        
        if( bodyStartIndex<0 || bodyEndIndex<0
           || bodyStartIndex > bodyEndIndex
           || bodyStartIndex>=textLength || bodyEndIndex>=textLength ){

            if( lcText.indexOf("<body/>")!=-1 || lcText.indexOf("<body />")!=-1 )
                return "";
            
            int htmlStartIndex = lcText.indexOf("<html>");
            int htmlEndIndex = lcText.indexOf("</html>");
            if( htmlStartIndex != -1 && htmlEndIndex > htmlStartIndex )
                return html.substring(htmlStartIndex+6, htmlEndIndex);
            
            if( isTypeDocument() )
                log.warn("Couldn't extract HTML body from :\n"+html);
            return html.trim();
        }

        return html.substring(bodyStartIndex, bodyEndIndex+1).trim();
    }
    
    protected enum PropertyKeys
    {
        fallback,
        type,
        allowEditSource,
        allowExternalLinks,
        addKupuLogo,
        showAllToolBoxes,
        showPropertiesToolBox,
        showLinksToolBox,
        showImagesToolBox,
        showTablesToolBox,
        showCleanupExpressionsToolBox,
        showDebugToolBox,
        uniqueIdCounter
    }
}
