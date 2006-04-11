/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/



package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.model.SelectItem;

import org.sakaiproject.jsf.util.ConfigurationResource;
import org.sakaiproject.jsf.util.RendererUtil;
import org.sakaiproject.jsf.model.InitObjectContainer;

import java.util.*;
import java.text.MessageFormat;

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

 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author cwen@iu.edu
 * @author Ed Smiley esmiley@stanford.edu (modifications)
 * @version $Id$
 */
public class InputRichTextRenderer extends Renderer
{
  private static final String SCRIPT_PATH;
  private static final String HTMLAREA_SCRIPT_PATH;
  private static final String RESOURCE_PATH;
  private static final String TOOLBAR_SCRIPT_NONE;
  private static final String TOOLBAR_SCRIPT_SMALL;
  private static final String TOOLBAR_SCRIPT_MEDIUM;
  
  
  
  private static final String TOOLBAR_SCRIPT_LARGE;
  private static final int DEFAULT_WIDTH_PX;
  private static final int DEFAULT_HEIGHT_PX;
  private static final int DEFAULT_COLUMNS;
  private static final int DEFAULT_ROWS;
  private static final String INSERT_IMAGE_LOC;
  private static final MessageFormat LIST_ITEM_FORMAT =
    new MessageFormat(",\"{0}\" : \"<a href=''{1}''>{0}</a>\"");


  // we have static resources for our script path and built-in toolbars etc.
  static {
    ConfigurationResource cr = new ConfigurationResource();
    SCRIPT_PATH = cr.get("inputRichTextScript");
    HTMLAREA_SCRIPT_PATH = cr.get("inputRichTextHTMLArea");
    RESOURCE_PATH = cr.get("resources");
    TOOLBAR_SCRIPT_NONE = makeToolbarScript(cr.get("inputRichText_none"));
    TOOLBAR_SCRIPT_SMALL = makeToolbarScript(cr.get("inputRichText_small"));
    TOOLBAR_SCRIPT_MEDIUM = makeToolbarScript(cr.get("inputRichText_medium"));
    TOOLBAR_SCRIPT_LARGE = makeToolbarScript(cr.get("inputRichText_large"));
    DEFAULT_WIDTH_PX = Integer.parseInt(cr.get("inputRichTextDefaultWidthPx"));
    DEFAULT_HEIGHT_PX = Integer.parseInt(cr.get("inputRichTextDefaultHeightPx"));
    DEFAULT_COLUMNS = Integer.parseInt(cr.get("inputRichTextDefaultTextareaColumns"));
    DEFAULT_ROWS = Integer.parseInt(cr.get("inputRichTextDefaultTextareaRows"));
    INSERT_IMAGE_LOC = "/" + RESOURCE_PATH + "/" + cr.get("inputRichTextFileInsertImage");
  }

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.component.InputRichTextComponent);
  }

  public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException
  {

    if (!component.isRendered())
    {
      return;
    }

    String contextPath =
      context.getExternalContext().getRequestContextPath() + SCRIPT_PATH;
    String clientId = component.getClientId(context);

    ResponseWriter writer = context.getResponseWriter();

    String value = null;
    if (component instanceof UIInput)
        value = (String) ((UIInput) component).getSubmittedValue();
    if (value == null && component instanceof ValueHolder)
        value = (String) ((ValueHolder) component).getValue();

    ///////////////////////////////////////////////////////////////////////////
    // attributes
    ///////////////////////////////////////////////////////////////////////////
//  Width of the widget (in pixel units).
//  If this attribute is not specified, the width is controlled by the 'cols' attribute.
    String width = (String) RendererUtil.getAttribute(context, component, "width");
//  Height of the widget (in pixel units).
//  If this attribute is not specified, the width is controlled by the 'rows' attribute.
    String height = (String) RendererUtil.getAttribute(context, component, "height");
//  If true, only the textarea will be rendered.  Defaults to false.
//  If true, the rich text toolbar  and external HTMLArea JavaScript will NOT.
    String textareaOnly = (String) RendererUtil.getAttribute(context, component, "textareaOnly");
//    Specify toolbar from among a number precanned lists of command buttons.
//    Allowed values are: "none", "small", "medium", "large".
     String buttonSet = (String) RendererUtil.getAttribute(context, component, "buttonSet");
//  Comma delimited list of toolbar command buttons registered with component.
    String buttonList = (String) RendererUtil.getAttribute(context, component, "buttonList");
    /**
     * @todo need to do something with extensions.
     */
//  URL to an external JavaScript file with additional JavaScript
    String javascriptLibraryExtensionURL =
        (String) RendererUtil.getAttribute(context, component, "javascriptLibraryExtensionURL");
//   The URL to the directory of the HTMLArea JavaScript library.
    String javascriptLibraryURL =
        (String) RendererUtil.getAttribute(context, component, "javascriptLibraryURL");
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
      Integer cols = (Integer) RendererUtil.getAttribute(context, component, "cols");
      Integer rows = (Integer) RendererUtil.getAttribute(context, component, "rows");
      if (cols != null) textareaColumns = cols.intValue();
      if (rows != null) textareaRows = rows.intValue();
    }
    catch (Exception ex)
    {
      //default, whatever goes awry
    }

    widthPx = (DEFAULT_WIDTH_PX*textareaColumns)/DEFAULT_COLUMNS;
    heightPx = (DEFAULT_HEIGHT_PX*textareaRows)/DEFAULT_ROWS;

    Locale locale = Locale.getDefault();

   // Render JavaScripts.
   writeExternalScripts(locale, writer);

   // Render base textarea.
   writeTextArea(clientId, value, textareaRows, textareaColumns, writer);

   // Make textarea rich text (unless textareaOnly is true).
   if (!"true".equals(textareaOnly))
   {
     String toolbarScript;

     if (buttonList != null)
     {
       toolbarScript = makeToolbarScript(buttonList);
     }
     else
     {
       toolbarScript = getStandardToolbarScript(buttonSet);
     }

     // hook up configuration object
     writeConfigurationScript(context, component, clientId, toolbarScript, widthPx, heightPx, showXPath, locale, writer);
   }

  }

  /**
   * Return the config script portion for a standard button set.
   * @param buttonSet
   * @return
   */
  private String  getStandardToolbarScript(String buttonSet) {
    String toolbarScript;
    if ("none".equals(buttonSet))
    {
      toolbarScript = TOOLBAR_SCRIPT_NONE;
    }
    else if ("small".equals(buttonSet))
    {
      toolbarScript = TOOLBAR_SCRIPT_SMALL;
    }
    else if ("medium".equals(buttonSet))
    {
      toolbarScript = TOOLBAR_SCRIPT_MEDIUM;
    }
    else if ("large".equals(buttonSet))
    {
      toolbarScript = TOOLBAR_SCRIPT_LARGE;
    }
    else
    {
      toolbarScript = TOOLBAR_SCRIPT_MEDIUM;
    }

    return toolbarScript;
  }

  /**
   * Write out HTML rextarea
   * @param clientId
   * @param value the textarrea value
   * @param writer
   * @throws IOException
   */
  private void writeTextArea(String clientId, String value, int rows, int cols,
    ResponseWriter writer) throws IOException {

  	// wrap the textarea in a table, so that the HTMLArea toolbar doesn't bleed to the
  	// edge of the screen.
  	writer.write("<table border=\"0\"><tr><td>\n");
    writer.write("<textarea name=\"");
    writer.write(clientId);
    writer.write("_inputRichText\" id=\"");
    writer.write(clientId);
    writer.write("_inputRichText\"");
    writer.write(" rows=\"" + rows + "\"");
    writer.write(" cols=\"" + cols + "\"");
    writer.write(">" + value + "</textarea>\n");
    writer.write("</td></tr></table>\n");
  }

  /**
   * @todo do these as a document.write after testing if done
   * @param contextPath
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
    if (!Locale.ENGLISH.equals(language))
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
        || !(component instanceof org.sakaiproject.jsf.component.InputRichTextComponent))
    {
      throw new IllegalArgumentException();
    }

    String clientId = component.getClientId(context);

    Map requestParameterMap = context.getExternalContext()
        .getRequestParameterMap();

    String newValue = (String) requestParameterMap.get(clientId + "_inputRichText");

    org.sakaiproject.jsf.component.InputRichTextComponent comp = (org.sakaiproject.jsf.component.InputRichTextComponent) component;
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
    String configVar = "config" + ("" + Math.random()).substring(2);

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

   protected void writeAttachedFiles(FacesContext context, UIComponent component, String configVar, ResponseWriter writer, String toolbar) throws IOException {
      Object attchedFiles = RendererUtil.getAttribute(context,  component, "attachedFiles");
      if (attchedFiles != null && getSize(attchedFiles) > 0) {
         String arrayVar = configVar + "_Resources";

         writer.write("  var " + arrayVar + "= {\n");
         writer.write("\"select a file url to insert\" : \"\"\n");

         if (attchedFiles instanceof Map) {
            writer.write(outputFiles((Map)attchedFiles));
         }
         else {
            writer.write(outputFiles((List)attchedFiles));
         }

         writer.write("};\n");

         writer.write(  "sakaiRegisterResourceList(");
         writer.write(configVar + ",'" + INSERT_IMAGE_LOC + "'," + arrayVar);
         writer.write(");\n");

         writer.write("  " + configVar + ".toolbar = " + addToolbar(toolbar) + ";\n");
      }
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

   protected String outputFiles(Map map) {
      StringBuffer sb = new StringBuffer();

      for (Iterator i=map.entrySet().iterator();i.hasNext();) {
         Map.Entry entry = (Map.Entry)i.next();

         LIST_ITEM_FORMAT.format(new Object[]{entry.getValue(), entry.getKey()}, sb, null);
      }

      return sb.toString();
   }

   protected String outputFiles(List list) {
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

         LIST_ITEM_FORMAT.format(new Object[]{label, url}, sb, null);
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
      toolbar = toolbar.substring(0, pos) +
         ",[\"filedropdown\", \"insertfile\", ]" +
         toolbar.substring(pos);
      return toolbar;
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
    StringBuffer script = new StringBuffer();
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

}