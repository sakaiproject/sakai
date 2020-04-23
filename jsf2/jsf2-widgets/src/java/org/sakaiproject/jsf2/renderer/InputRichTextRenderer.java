/**********************************************************************************
Copyright (c) 2018 Apereo Foundation

Licensed under the Educational Community License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************************/

package org.sakaiproject.jsf2.renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;
import javax.faces.render.Renderer;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.jsf2.model.InitObjectContainer;
import org.sakaiproject.jsf2.util.ConfigurationResource;
import org.sakaiproject.jsf2.util.RendererUtil;

/**
 * <p>Formerly RichTextEditArea.java</p>
 * <p>Renders a rich text editor and toolbar within an HTML "textarea" element.</p>
    <p>The textarea is decorated using the HTMLArea JavaScript library.</p>
    <p>
      HTMLArea is a free, customizable online editor.  It works inside your
      browser.  It uses a non-standard feature implemented in Internet
      Explorer 5.5 or better for Windows and Mozilla 1.3 or better (any
      platform), therefore it will only work in one of these browsers.
    </p>

    <p>
      HTMLArea is copyright <a
      href="http://interactivetools.com">InteractiveTools.com</a> and
      released under a BSD-style license.  HTMLArea is created and developed
      upto version 2.03 by InteractiveTools.com.  Version 3.0 developed by
      <a href="http://students.infoiasi.ro/~mishoo/">Mihai Bazon</a> for
      InteractiveTools.  It contains code sponsored by other companies as
      well.
    </p>
 */
@Slf4j
public class InputRichTextRenderer extends Renderer
{
  private static final String SCRIPT_PATH;
  private static final String HTMLAREA_SCRIPT_PATH;
  private static final String RESOURCE_PATH;

  private static final int DEFAULT_WIDTH_PX;
  private static final int DEFAULT_HEIGHT_PX;
  private static final int DEFAULT_COLUMNS;
  private static final int DEFAULT_ROWS;
  private static final String INSERT_IMAGE_LOC;
  private static final MessageFormat LIST_ITEM_FORMAT_HTML =
     new MessageFormat("\"{0}\" : \"<a href=''{1}''>{0}</a>\"");

  // we have static resources for our script path and built-in toolbars etc.
  static {
    ConfigurationResource cr = new ConfigurationResource();
    SCRIPT_PATH = cr.get("inputRichTextScript");
    HTMLAREA_SCRIPT_PATH = cr.get("inputRichTextHTMLArea");
    RESOURCE_PATH = cr.get("resources");
    DEFAULT_WIDTH_PX = Integer.parseInt(cr.get("inputRichTextDefaultWidthPx").trim());
    /*SAK-20809 if an erant white space is left after the value this could throw a
     * number format exception so we trim it
    */
    DEFAULT_HEIGHT_PX = Integer.parseInt(cr.get("inputRichTextDefaultHeightPx").trim());
    DEFAULT_COLUMNS = Integer.parseInt(cr.get("inputRichTextDefaultTextareaColumns").trim());
    DEFAULT_ROWS = Integer.parseInt(cr.get("inputRichTextDefaultTextareaRows").trim());
    INSERT_IMAGE_LOC = "/" + RESOURCE_PATH + "/" + cr.get("inputRichTextFileInsertImage");
  }

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf2.component.InputRichTextComponent);
  }

  public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException
  {

    if (!component.isRendered())
    {
      return;
    }

    String clientId = component.getClientId(context);

    ResponseWriter writer = context.getResponseWriter();

    String value = null;
    if (component instanceof UIInput)
        value = (String) ((UIInput) component).getSubmittedValue();
    if (value == null && component instanceof ValueHolder)
        value = (String) ((ValueHolder) component).getValue();
    // SAK-23313
    // The rich-text editor will interpret a string like &lt;tag&gt; as a real tag
    // So we double-escape the ampersand to create &amp;lt; so CKEditor displays this as text
    if (value!=null) {
    	java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("&([^\\s])");
    	value = pattern.matcher(value).replaceAll("&amp;$1");
    }

    ///////////////////////////////////////////////////////////////////////////
    // attributes
    ///////////////////////////////////////////////////////////////////////////

//  If true, only the textarea will be rendered.  Defaults to false.
//  If true, the rich text toolbar  and external HTMLArea JavaScript will NOT.
    String textareaOnly = (String) RendererUtil.getAttribute(context, component, "textareaOnly");

    //If true, send full documents via CKEditor
    String enableFullPage = (String) RendererUtil.getAttribute(context, component, "enableFullPage");

//  If true show XPath at bottom of editor.  Default is true.
    String showXPath = (String) RendererUtil.getAttribute(context, component, "showXPath");
    showXPath = RendererUtil.makeSwitchString(showXPath, true, true, true, false, false, true);

    ///////////////////////////////////////////////////////////////////////////

    // set up dimensions
    int widthPx = DEFAULT_WIDTH_PX;
    int heightPx = DEFAULT_HEIGHT_PX;
    int textareaColumns = DEFAULT_COLUMNS;
    int textareaRows = DEFAULT_ROWS;

    try
    {
      Integer cols = Integer.parseInt("" + RendererUtil.getAttribute(context, component, "cols"));
      Integer rows = Integer.parseInt("" + RendererUtil.getAttribute(context, component, "rows"));
      if (cols != null) textareaColumns = cols.intValue();
      if (rows != null) textareaRows = rows.intValue();
      
      // Width of the widget (in pixel units).
      // If this attribute is not specified, the width is controlled by the 'cols' attribute.
      Integer width = (Integer) RendererUtil.getAttribute(context, component, "width");
      if (width != null) widthPx = width.intValue();

      // Height of the widget (in pixel units).
      // If this attribute is not specified, the width is controlled by the 'rows' attribute.
      Integer height = (Integer) RendererUtil.getAttribute(context, component, "height");
      if (height != null) heightPx = height.intValue();
    }
    catch (Exception ex)
    {
      //default, whatever goes awry
      log.debug(ex.getMessage());
    }

    if (widthPx == DEFAULT_WIDTH_PX && textareaColumns != DEFAULT_COLUMNS)
       widthPx = (DEFAULT_WIDTH_PX*textareaColumns)/DEFAULT_COLUMNS;
    if (heightPx == DEFAULT_HEIGHT_PX && textareaRows != DEFAULT_ROWS)
       heightPx = (DEFAULT_HEIGHT_PX*textareaRows)/DEFAULT_ROWS;

    String collectionBase = (String) RendererUtil.getAttribute(context, component, "collectionBase");
    String collectionId = "";
    if (collectionBase != null) {
        collectionId="collectionId: '"+collectionBase.replaceAll("\"","\\\"")+"'";
    }
    
    writer.write("<table border=\"0\"><tr><td>");
    writer.write("<textarea name=\"" + clientId + "_inputRichText\" id=\"" + clientId + "_inputRichText\"");
    if (textareaColumns > 0) writer.write(" cols=\""+textareaColumns+"\"");
    if (textareaRows > 0) writer.write(" rows=\""+textareaRows+"\"");
    writer.write(">");
    if (value != null)
       writer.write((String) value);
    writer.write("</textarea>");
    
    if (!"true".equals(textareaOnly))
		{
			if (enableFullPage != null && "true".equals(enableFullPage))
			{
				writer.write("<script type=\"text/javascript\" defer=\"1\">");
				writer.write("function config(){}");
				writer.write("config.prototype.fullPage=true;");
				writer.write("config.prototype.width=" + widthPx + ";");
				writer.write("config.prototype.height=" + heightPx + ";");
				if (collectionBase != null) writer.write("config.prototype.collectionId='" + collectionBase.replaceAll("\"", "\\\"") + "';");
				writer.write("sakai.editor.launch('" + clientId + "_inputRichText', new config(), " + widthPx + ", " + heightPx + ");</script>");

			}
			else
			{
				writer.write("<script type=\"text/javascript\">sakai.editor.launch('" + clientId + "_inputRichText', {" + collectionId + "}, '"
						+ widthPx + "','" + heightPx + "');</script>");
			}
		}

    writer.write("</td></tr></table>\n");

  }


  /**
   * @todo do these as a document.write after testing if done
   * @param locale
   * @param writer
   * @throws IOException
   */
  protected void writeExternalScripts(Locale locale, ResponseWriter writer)
      throws IOException {
    writer.write("<script type=\"text/javascript\">var _editor_url = \"" +
                 "/" + RESOURCE_PATH + "/" + HTMLAREA_SCRIPT_PATH + "/" +
                 "\";</script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + "/" +
                 RESOURCE_PATH + "/" + HTMLAREA_SCRIPT_PATH + "/" +
                 "htmlarea.js\"></script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + "/" +
                 RESOURCE_PATH + "/" + HTMLAREA_SCRIPT_PATH + "/" +
                 "dialog.js\"></script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + "/" +
                 RESOURCE_PATH + "/" + HTMLAREA_SCRIPT_PATH + "/" +
                 "popupwin.js\"></script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + "/" +
                 RESOURCE_PATH + "/" + HTMLAREA_SCRIPT_PATH + "/" +
                 "lang/en.js\"></script>\n");

    String language = locale.getLanguage();
    if (!Locale.ENGLISH.getLanguage().equals(language))
    {
      writer.write("<script type=\"text/javascript\" src=\"" + "/" +
        RESOURCE_PATH + "/"     + HTMLAREA_SCRIPT_PATH + "/" +
        "lang/" + language + ".js\"></script>\n");
    }
    writer.write("<script type=\"text/javascript\" src=\"" + "/" +
      RESOURCE_PATH + "/" + SCRIPT_PATH + "\"></script>\n");
  }

  /**
   * Standard decode method.
   * @param context
   * @param component
   */
  public void decode(FacesContext context, UIComponent component)
  {
    if( RendererUtil.isDisabledOrReadonly(context, component)) return;

    if (null == context || null == component
        || !(component instanceof org.sakaiproject.jsf2.component.InputRichTextComponent))
    {
      throw new IllegalArgumentException();
    }

    String clientId = component.getClientId(context);

    Map requestParameterMap = context.getExternalContext()
        .getRequestParameterMap();

    String newValue = (String) requestParameterMap.get(clientId + "_inputRichText");

    org.sakaiproject.jsf2.component.InputRichTextComponent comp = (org.sakaiproject.jsf2.component.InputRichTextComponent) component;
    comp.setSubmittedValue(newValue);
  }

  /**
   * Write configuration script
   *
   * @param clientId the client id
   * @param toolbar the toolbar configuration string (i.e from makeToolbarScript())
   * @param widthPx columns
   * @param heightPx rows
   */
  protected void writeConfigurationScript(FacesContext context, UIComponent component, String clientId,
    String toolbar, int widthPx, int heightPx, String showXPath, Locale locale, ResponseWriter writer)
    throws IOException
  {
    // script creates unique Config object
    String configVar = "config" + createSafeRandomNumber();

    writer.write("<script type=\"text/javascript\">\n");
    writer.write("  sakaiSetLanguage(\"" + locale.getDisplayLanguage() + "\");");
    writer.write("  var " + configVar + "=new HTMLArea.Config();\n");
    writer.write("  sakaiRegisterButtons(" + configVar + ");\n");
    writer.write("  " + configVar + ".toolbar = " + toolbar + ";\n");
    writer.write("  " + configVar + ".width=\"" + widthPx + "px\";\n");
    writer.write("  " + configVar + ".height=\"" + heightPx + "px\";\n");
    writer.write("  " + configVar + ".statusBar=" + showXPath + ";\n");
    writeAdditionalConfig(context, component, configVar, clientId,
          toolbar, widthPx, heightPx, locale,  writer);
    writer.write(  "sakaiSetupRichTextarea(\"");
    writer.write(clientId);
    writer.write("_inputRichText\"," + configVar + ");\n");
    writer.write("</script>\n");
  }

   /**
    * subclasses can override to provide additonal configuration such as add buttons, etc
    * @param context
    * @param component
    * @param configVar
    * @param clientId
    * @param toolbar
    * @param widthPx
    * @param heightPx
    * @param locale
    * @param writer
    */
   protected void writeAdditionalConfig(FacesContext context, UIComponent component, String configVar,
      String clientId, String toolbar, int widthPx, int heightPx, Locale locale, ResponseWriter writer)
      throws IOException{
      writeAttachedFiles(context, component, configVar, writer, toolbar);
      registerWithParent(component, configVar, clientId);
   }

   protected void writeAttachedFiles(FacesContext context, UIComponent component,
                                     String configVar, ResponseWriter writer, String toolbar) throws IOException {
      Object attchedFiles = RendererUtil.getAttribute(context,  component, "attachedFiles");
      if (attchedFiles != null && getSize(attchedFiles) > 0) {
         String arrayVar = configVar + "_Resources";

         writeFilesArray(writer, arrayVar, attchedFiles, LIST_ITEM_FORMAT_HTML, true);

         writer.write(  "sakaiRegisterResourceList(");
         writer.write(configVar + ",'" + INSERT_IMAGE_LOC + "'," + arrayVar);
         writer.write(");\n");

         writer.write("  " + configVar + ".toolbar = " + addToolbar(toolbar) + ";\n");
      }
   }

   protected void writeFilesArray(ResponseWriter writer, String arrayVar,
                                  Object attchedFiles, MessageFormat format,
                                  boolean includeLabel) throws IOException {
      StringWriter buffer = new StringWriter();

      char startChar = '[';
      char endChar = ']';

      if (LIST_ITEM_FORMAT_HTML.equals(format)) {
         startChar = '{';
         endChar = '}';
      }

      buffer.write("  var " + arrayVar + " = "+startChar+"\n");

      if (includeLabel) {
         buffer.write("\"select a file url to insert\" : \"\"");
      }

      if (attchedFiles instanceof Map) {
         buffer.write(outputFiles((Map)attchedFiles, format, !includeLabel));
      }
      else {
         buffer.write(outputFiles((List)attchedFiles, format, !includeLabel));
      }

      buffer.write(endChar + ";\n");
      String result = buffer.toString();
      writer.write(result);
   }

   protected void registerWithParent(UIComponent component, String configVar, String clientId) {

      InitObjectContainer parentContainer = null;

      UIComponent testContainer = component.getParent();
      while (testContainer != null) {
         if (testContainer instanceof InitObjectContainer) {
            parentContainer = (InitObjectContainer)testContainer;

            String script = " resetRichTextEditor(\"" + clientId +
               "_inputRichText\"," + configVar + ");\n";

            parentContainer.addInitScript(script);
         }
         testContainer = testContainer.getParent();
      }
   }

   protected String outputFiles(Map map, MessageFormat format, boolean first) {
	   StringBuffer sb = new StringBuffer();

      for (Iterator i=map.entrySet().iterator();i.hasNext();) {
         Map.Entry entry = (Map.Entry)i.next();
         if (!first) {
            sb.append(',');
         }
         else {
            first = false;
         }
         format.format(new Object[]{entry.getValue(), entry.getKey()}, sb, null);
      }

      return sb.toString();
   }

   protected String outputFiles(List list, MessageFormat format, boolean first) {
	   StringBuffer sb = new StringBuffer();

      for (Iterator i=list.iterator();i.hasNext();) {
         Object value = i.next();

         String url;
         String label;

         if (value instanceof SelectItem) {
            SelectItem item = (SelectItem)value;
            url = item.getValue().toString();
            label = item.getLabel();
         }
         else {
            url = value.toString();
            label = value.toString();
         }

         if (!first) {
            sb.append(',');
         }
         else {
            first = false;
         }
         format.format(new Object[]{label, url}, sb, null);
      }

      return sb.toString();
   }

   protected int getSize(Object attchedFiles) {
      if (attchedFiles instanceof Map) {
         return ((Map)attchedFiles).size();
      }
      else {
         return ((List)attchedFiles).size();
      }
   }

   protected String addToolbar(String toolbar) {
      int pos = toolbar.lastIndexOf("]");
      String toolbarResponse = toolbar.substring(0, pos) +
         ",[\"filedropdown\", \"insertfile\", ]" +
         toolbar.substring(pos);
      return toolbarResponse;
   }

   /**
   * Built toolbar part of configuration script for a list of button commands.
   *
   * @param buttonList csv list of buttons
   * @return String, e.g.
   * <code><pre>
   *    [["fontname", "space",... ]] etc.
   * </pre></code>
   *
   */
  private static String makeToolbarScript(String buttonList) {
    StringBuilder script = new StringBuilder();
    String q = "\"";

    script.append("[[");

    StringTokenizer st = new StringTokenizer(buttonList, ",", false);

    while (st.hasMoreTokens())
    {
      String command = st.nextToken();
      if (!"linebreak".equals(command))
      {
        script.append(q + command + q + ", ");
      }
      else
      {
        script.append("],[");
      }
    }

    script.append("]]");
    return script.toString();
  }
  
  private String createSafeRandomNumber() {
     return "" + (long)(Math.floor(Math.random() * 1000000000));
  }

}
