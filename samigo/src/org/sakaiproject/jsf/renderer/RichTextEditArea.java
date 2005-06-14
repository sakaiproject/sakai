/*
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
 */

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 *
 * <p>Quick port of Sakai rich text editor to Samigo </p>
 * <p>Slight differences in path definition </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author cwen@iu.edu
 * @author Ed Smiley esmiley@stanford.edu (modifications)
 * @version $Id: RichTextEditArea.java,v 1.5 2005/06/08 01:22:54 esmiley.stanford.edu Exp $
 */
public class RichTextEditArea extends Renderer
{
  private static final String SCRIPT_PATH = "/jsf/widget/wysiwyg/htmlarea/";
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.component.
            RichTextEditArea);
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws
    IOException
  {
    String contextPath =
      context.getExternalContext().getRequestContextPath() + SCRIPT_PATH;
    String clientId = component.getClientId(context);

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
      outRow = "80px";
      outCol = "450px";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col < 450))
    {
      outRow = new Integer(row).toString() + "px";
      outCol = "450px";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col >= 450) && (col < 630))
    {
      outRow = new Integer(row).toString() + "px";
      outCol = new Integer(col).toString() + "px";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col >= 630))
    {
      outRow = new Integer(row).toString() + "px";
      outCol = new Integer(col).toString() + "px";
      lineOfToolBar = 2;
    }
    else
    if ( (row < 80) && (col >= 630))
    {
      outRow = "80px";
      outCol = new Integer(col).toString() + "px";
      lineOfToolBar = 2;
    }
    else
    {
      outRow = "80px";
      outCol = new Integer(col).toString() + "px";
      lineOfToolBar = 3;
    }

    String justArea = (String) component.getAttributes().get("justArea");

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

  public void decode(FacesContext context, UIComponent component)
  {
    if (null == context || null == component
        ||
        ! (component instanceof org.sakaiproject.jsf.component.RichTextEditArea))
    {
      throw new IllegalArgumentException();
    }

    String clientId = component.getClientId(context);

    Map requestParameterMap = context.getExternalContext()
      .getRequestParameterMap();

    String newValue = (String) requestParameterMap.get(clientId + "_textinput");

    org.sakaiproject.jsf.component.RichTextEditArea comp = (org.sakaiproject.
      jsf.component.RichTextEditArea) component;
    comp.setSubmittedValue(newValue);
  }
}