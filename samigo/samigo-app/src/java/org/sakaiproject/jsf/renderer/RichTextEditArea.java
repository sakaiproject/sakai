/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.ToolManager; 
import org.sakaiproject.content.cover.ContentHostingService; 
import org.sakaiproject.util.FormattedText;


/**
 *
 * <p>Quick port of the Sakai rich text editor from htmlarea to FCKeditor </p>
 * <p>Slight differences in path definition </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author cwen@iu.edu
 * @author Ed Smiley esmiley@stanford.edu (modifications)
 * @author Joshua Ryan joshua.ryan@asu.edu (added FCKEditor)
 * @version $Id$
 */
public class RichTextEditArea extends Renderer
{
  //FCK config paths
  private static final String FCK_BASE = "/library/editor/FCKeditor/";
  private static final String FCK_SCRIPT = "fckeditor.js";

  //htmlarea script path
  private static final String SCRIPT_PATH = "/jsf/widget/wysiwyg/htmlarea/";

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.component.
            RichTextEditArea);
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws
    IOException
  {
    String editor = ServerConfigurationService.getString("wysiwyg.editor");

    String clientId = component.getClientId(context);

    String contextPath =
      context.getExternalContext().getRequestContextPath() + SCRIPT_PATH;

    ResponseWriter writer = context.getResponseWriter();

    Object value = null;
    if (component instanceof UIInput)
    {
      value = ( (UIInput) component).getSubmittedValue();
    }
    if (value == null && component instanceof ValueHolder)
    {
      value = ( (ValueHolder) component).getValue();

    }

    //fixes SAK-3116, I'm not sure if this logic really belongs in a renderer, but it 
    //need to happen before the wysiwig is written or the editor tries to be too smart 
    value = FormattedText.escapeHtmlFormattedTextarea((String) value);

    String tmpCol = (String) component.getAttributes().get("columns");
    String tmpRow = (String) component.getAttributes().get("rows");
    int col;
    int row;
    if (tmpCol != null)
    {
      col = new Integer(tmpCol).intValue();
    }
    else
    {
      col = 450;
    }
    if (tmpRow != null)
    {
      row = new Integer(tmpRow).intValue();
    }
    else
    {
      row = 80;

    }
    String outCol;
    String outRow;
    int lineOfToolBar;
    if ( (row < 80) && (col < 450))
    {
      outRow = "80";
      outCol = "450";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col < 450))
    {
      outRow = Integer.toString(row);
      outCol = "450";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col >= 450) && (col < 630))
    {
      outRow = Integer.toString(row);
      outCol = Integer.toString(col);
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col >= 630))
    {
      outRow = Integer.toString(row);
      outCol = Integer.toString(col);
      lineOfToolBar = 2;
    }
    else
    if ( (row < 80) && (col >= 630))
    {
      outRow = "80";
      outCol = Integer.toString(col);
      lineOfToolBar = 2;
    }
    else
    {
      outRow = "80";
      outCol = new Integer(col).toString();
      lineOfToolBar = 3;
    }

    String justArea = (String) component.getAttributes().get("justArea");

    if (editor.equals("FCKeditor")) {
      encodeFCK(writer, contextPath, (String) value, outCol, 
              outRow, justArea, clientId); 
    }
    else 
    {
      encodeHtmlarea(writer, contextPath, (String) value, outCol + "px", outRow + "px", 
              tmpCol, tmpRow, lineOfToolBar, justArea, clientId);
    }
  }


  private void encodeHtmlarea(ResponseWriter writer, String contextPath, String value, String outCol, 
           String outRow, String tmpCol, String tmpRow, int lineOfToolBar, String justArea, String clientId) throws IOException
  {

    writer.write("<script type=\"text/javascript\">var _editor_url = \"" +
                 contextPath + "\";</script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + contextPath +
                 "htmlarea.js\"></script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + contextPath +
                 "dialog.js\"></script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + contextPath +
                 "popupwin.js\"></script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + contextPath +
                 "lang/en.js\"></script>\n");

    if ( (justArea != null) && (justArea.equals("yes")))
    {
      writer.write("<textarea name=\"");
      writer.write(clientId);
      writer.write("_textinput\" id=\"");
      writer.write(clientId);
      writer.write("_textinput\" disabled>");
      writer.write( (String) value);
      writer.write("</textarea>\n");
      if ( (tmpCol != null) && (tmpRow != null))
      {
        writer.write(
          "<script type=\"text/javascript\">var config=new HTMLArea.Config();");
        writer.write("config.toolbar=[];config.width=\'");
        writer.write(tmpCol);
        writer.write("px\';config.height=\'");
        writer.write(tmpRow);
        writer.write("px\';");

        writer.write("var editor = HTMLArea.replace(\'");
        writer.write(clientId);
        writer.write("_textinput\',config);\n");

        writer.write("  if(document.htmlareas==undefined)");
        writer.write("  {");
        writer.write("   document.htmlareas = new Array();");
        writer.write("  }");
        writer.write("  var counter = document.htmlareas.length;");
        writer.write("  var textareaId = '" + clientId + "_textinput';");
        writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
        writer.write("</script>\n");
      }
      else
      if (tmpCol != null)
      {
        writer.write(
          "<script type=\"text/javascript\">var config=new HTMLArea.Config();");
        writer.write("config.toolbar=[];config.width=\'");
        writer.write(tmpCol);
        writer.write("px\';config.height=\'200px\';");
        writer.write("var editor = HTMLArea.replace(\'");
        writer.write(clientId);
        writer.write("_textinput\',config);\n");

        writer.write("  if(document.htmlareas==undefined)");
        writer.write("  {");
        writer.write("   document.htmlareas = new Array();");
        writer.write("  }");
        writer.write("  var counter = document.htmlareas.length;");
        writer.write("  var textareaId = '" + clientId + "_textinput';");
        writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
        writer.write("</script>\n");
      }
      else
      {
        writer.write(
          "<script type=\"text/javascript\">var config=new HTMLArea.Config();");
        writer.write(
          "config.toolbar=[];config.width=\'400px\';config.height=\'100px\';");
        writer.write("var editor = HTMLArea.replace(\'");
        writer.write("_textinput\',config);\n");

        writer.write("  if(document.htmlareas==undefined)");
        writer.write("  {");
        writer.write("   document.htmlareas = new Array();");
        writer.write("  }");
        writer.write("  var counter = document.htmlareas.length;");
        writer.write("  var textareaId = '" + clientId + "_textinput';");
        writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
        writer.write("</script>\n");
      }
    }
    else
    {
      if (value == null)
      {
        writer.write("<textarea name=\"");
        writer.write(clientId);
        writer.write("_textinput\" id=\"");
        writer.write(clientId);
        writer.write("_textinput\"");
        writer.write("></textarea>\n");

        if (lineOfToolBar == 3)
        {
          writer.write(
            "<script type=\"text/javascript\"> var config=new HTMLArea.Config();");
          writer.write("config.toolbar = [[\'fontname\', \'space\',\'fontsize\', \'space\',\'formatblock\', \'space\',\'bold\', \'italic\', \'underline\'],");
          writer.write("[\'separator\',\'strikethrough\', \'subscript\', \'superscript\', \'separator\', \'space\', \'undo\', \'redo\', \'separator\', \'justifyleft\', \'justifycenter\', \'justifyright\', \'justifyfull\', \'separator\',\'outdent\', \'indent\'],");
          writer.write("[\'separator\',\'forecolor\', \'hilitecolor\', \'textindicator\', \'separator\',\'inserthorizontalrule\', \'createlink\', \'insertimage\', \'separator\',  \'showhelp\', \'about\' ],");
          writer.write("];config.width=\'");
          writer.write(outCol);
          writer.write("\';config.height=\'");
          writer.write(outRow);
          writer.write("\';var editor = HTMLArea.replace(\'");
          writer.write(clientId);
          writer.write("_textinput\',config);\n");

          writer.write("  if(document.htmlareas==undefined)");
          writer.write("  {");
          writer.write("   document.htmlareas = new Array();");
          writer.write("  }");
          writer.write("  var counter = document.htmlareas.length;");
          writer.write("  var textareaId = '" + clientId + "_textinput';");
          writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
          writer.write("</script>\n");

        }
        else
        {
          writer.write(
            "<script type=\"text/javascript\">var config=new HTMLArea.Config();");
          writer.write("config.toolbar = [[\'fontname\', \'space\',\'fontsize\', \'space\',\'formatblock\', \'space\',\'bold\', \'italic\', \'underline\',\'separator\',\'strikethrough\', \'subscript\', \'superscript\', \'separator\', \'space\', \'undo\', \'redo\'],");
          writer.write("[\'separator\', \'justifyleft\', \'justifycenter\', \'justifyright\', \'justifyfull\', \'separator\',\'outdent\', \'indent\',\'separator\',\'forecolor\', \'hilitecolor\', \'textindicator\', \'separator\',\'inserthorizontalrule\', \'createlink\', \'insertimage\', \'separator\',  \'showhelp\', \'about\' ],");
          writer.write("];config.width=\'");
          writer.write(outCol);
          writer.write("\';config.height=\'");
          writer.write(outRow);
          writer.write("\';var editor = HTMLArea.replace(\'");
          writer.write(clientId);
          writer.write("_textinput\',config);\n");

          writer.write("  if(document.htmlareas==undefined)");
          writer.write("  {");
          writer.write("   document.htmlareas = new Array();");
          writer.write("  }");
          writer.write("  var counter = document.htmlareas.length;");
          writer.write("  var textareaId = '" + clientId + "_textinput';");
          writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
          writer.write("</script>\n");
        }
      }
      else
      {
        writer.write("<textarea name=\"");
        writer.write(clientId);
        writer.write("_textinput\" id=\"");
        writer.write(clientId);
        writer.write("_textinput\">");
        writer.write( (String) value);
        writer.write("</textarea>\n");
        if (lineOfToolBar == 3)
        {
          writer.write(
            "<script type=\"text/javascript\"> var config=new HTMLArea.Config();");
          writer.write("config.toolbar = [[\'fontname\', \'space\',\'fontsize\', \'space\',\'formatblock\', \'space\',\'bold\', \'italic\', \'underline\'],");
          writer.write("[\'separator\',\'strikethrough\', \'subscript\', \'superscript\', \'separator\', \'space\', \'undo\', \'redo\', \'separator\', \'justifyleft\', \'justifycenter\', \'justifyright\', \'justifyfull\', \'separator\',\'outdent\', \'indent\'],");
          writer.write("[\'separator\',\'forecolor\', \'hilitecolor\', \'textindicator\', \'separator\',\'inserthorizontalrule\', \'createlink\', \'insertimage\', \'separator\',  \'showhelp\', \'about\' ],");
          writer.write("];config.width=\'");
          writer.write(outCol);
          writer.write("\';config.height=\'");
          writer.write(outRow);
          writer.write("\';var editor = HTMLArea.replace(\'");
          writer.write(clientId);
          writer.write("_textinput\',config);\n");

          writer.write("  if(document.htmlareas==undefined)");
          writer.write("  {");
          writer.write("   document.htmlareas = new Array();");
          writer.write("  }");
          writer.write("  var counter = document.htmlareas.length;");
          writer.write("  var textareaId = '" + clientId + "_textinput';");
          writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
          writer.write("</script>\n");

        }
        else
        {
          writer.write(
            "<script type=\"text/javascript\">var config=new HTMLArea.Config();");
          writer.write("config.toolbar = [[\'fontname\', \'space\',\'fontsize\', \'space\',\'formatblock\', \'space\',\'bold\', \'italic\', \'underline\',\'separator\',\'strikethrough\', \'subscript\', \'superscript\', \'separator\', \'space\', \'undo\', \'redo\'],");
          writer.write("[\'separator\', \'justifyleft\', \'justifycenter\', \'justifyright\', \'justifyfull\', \'separator\',\'outdent\', \'indent\',\'separator\',\'forecolor\', \'hilitecolor\', \'textindicator\', \'separator\',\'inserthorizontalrule\', \'createlink\', \'insertimage\',  \'separator\',  \'showhelp\', \'about\' ],");
          writer.write("];config.width=\'");
          writer.write(outCol);
          writer.write("\';");
          writer.write("config.height=\'");
          writer.write(outRow);
          writer.write("\';var editor = HTMLArea.replace(\'");
          writer.write(clientId);
          writer.write("_textinput\',config);\n");

          writer.write("  if(document.htmlareas==undefined)");
          writer.write("  {");
          writer.write("   document.htmlareas = new Array();");
          writer.write("  }");
          writer.write("  var counter = document.htmlareas.length;");
          writer.write("  var textareaId = '" + clientId + "_textinput';");
          writer.write("  document.htmlareas[counter] = new Array(textareaId, editor, false);");
          writer.write("</script>\n");
        }
      }
    }
  }

   
  private void encodeFCK(ResponseWriter writer, String contextPath, String value, String outCol, 
         String outRow, String justArea, String clientId) throws IOException
  {

    //fck's tool bar can get pretty big
    if (new Integer(outRow).intValue() < 300) 
    {
         outRow = (new Integer(outRow).intValue() + 100) + "";
    }

    writer.write("<textarea name=\"" + clientId + "_textinput\" id=\"" + clientId + "_textinput\">");
    writer.write((String) value);
    writer.write("</textarea>");

    writer.write("\n\t<script type=\"text/javascript\" src=\"" + FCK_BASE + FCK_SCRIPT + "\"></script>");

    writer.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
    writer.write("\n  if(document.wysiwyg==undefined)");
    writer.write("\n  {");
    writer.write("\n    document.wysiwyg = \"FCKeditor\"");
    writer.write("\n  }");
    writer.write("\n\nfunction chef_setupformattedtextarea(textarea_id){\n");

    writer.write("var oFCKeditor = new FCKeditor(textarea_id);\n");
    writer.write("\n\toFCKeditor.BasePath = \"" + FCK_BASE + "\";");
    writer.write("\n\toFCKeditor.Height = " + outRow + ";");
    writer.write("\n\n\toFCKeditor.Width = " + outCol + ";");

    if ( (justArea != null) && (justArea.equals("yes")))
    {
      writer.write("\n\toFCKeditor.ToolbarSet = \"plain\";");
    }
    else
    {

        String connector = "/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector";
	String collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext()); 


        if ("archival".equals(ServerConfigurationService.getString("tags.focus")))
             writer.write("\n\toFCKeditor.Config['CustomConfigurationsPath'] = \"/library/editor/FCKeditor/archival_config.js\";\n");
        else {
	  writer.write("\n\t\tvar courseId = \"" + collectionId + "\";"); 
          writer.write("\n\toFCKeditor.Config['ImageBrowserURL'] = oFCKeditor.BasePath + " + 
                "\"editor/filemanager/browser/default/browser.html?Connector=" + connector + "&Type=Image&CurrentFolder=\" + courseId;");
          writer.write("\n\toFCKeditor.Config['LinkBrowserURL'] = oFCKeditor.BasePath + " + 
                "\"editor/filemanager/browser/default/browser.html?Connector=" + connector + "&Type=Link&CurrentFolder=\" + courseId;");
          writer.write("\n\toFCKeditor.Config['FlashBrowserURL'] = oFCKeditor.BasePath + " +  
                "\"editor/filemanager/browser/default/browser.html?Connector=" + connector + "&Type=Flash&CurrentFolder=\" + courseId;");
          writer.write("\n\toFCKeditor.Config['ImageUploadURL'] = oFCKeditor.BasePath + " +  
                "\"" + connector + "?Type=Image&Command=QuickUpload&Type=Image&CurrentFolder=\" + courseId;");
          writer.write("\n\toFCKeditor.Config['FlashUploadURL'] = oFCKeditor.BasePath + " +  
                "\"" + connector + "?Type=Flash&Command=QuickUpload&Type=Flash&CurrentFolder=\" + courseId;");
          writer.write("\n\toFCKeditor.Config['LinkUploadURL'] = oFCKeditor.BasePath + " +  
                "\"" + connector + "?Type=File&Command=QuickUpload&Type=Link&CurrentFolder=\" + courseId;");
	
          writer.write("\n\n\toFCKeditor.Config['CurrentFolder'] = courseId;");

          writer.write("\n\toFCKeditor.Config['CustomConfigurationsPath'] = \"/library/editor/FCKeditor/config.js\";\n");

        }

    }
    
    writer.write("\n\n\toFCKeditor.ReplaceTextarea();\n\t}\n</script>");

    writer.write("<script type=\"text/javascript\" defer=\"1\">chef_setupformattedtextarea('" + clientId + "_textinput');</script>");

  }

  public void decode(FacesContext context, UIComponent component)
  {
    if (null == context || null == component ||
        ! (component instanceof org.sakaiproject.jsf.component.RichTextEditArea))
    {
      throw new IllegalArgumentException();
    }

    String clientId = component.getClientId(context);

    Map requestParameterMap = context.getExternalContext()
      .getRequestParameterMap();

    String newValue = (String) requestParameterMap.get(clientId + "_textinput");

    org.sakaiproject.jsf.component.RichTextEditArea comp = (org.sakaiproject.jsf.component.RichTextEditArea) component;
    comp.setSubmittedValue(newValue);
  }
}
