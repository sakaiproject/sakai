/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.cover.ToolManager; 
import org.sakaiproject.util.EditorConfiguration;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;



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
  private static Log log = LogFactory.getLog(RichTextEditArea.class);	

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
    String reset = (String) component.getAttributes().get("reset");
    if (reset == null || !reset.equals("true")) {
    	if (component instanceof UIInput)
    	{
    		value = ( (UIInput) component).getSubmittedValue();
    	}
    	if (value == null && component instanceof ValueHolder)
    	{
    		value = ( (ValueHolder) component).getValue();
    	}
    }

    boolean valueHasRichText = false;
    	if((String) value != null){
    		String valueNoNewLine = ((String) value).replaceAll("\\n", "").replaceAll("\\r", "");
    		//really simple regex to detect presence of any html tags in the value        
    		valueHasRichText = Pattern.compile(".*<.*?>.*", Pattern.CASE_INSENSITIVE).matcher(valueNoNewLine).matches();
    		//valueHasRichText = Pattern.compile(".*(<)[^\n^<]+(>).*", Pattern.CASE_INSENSITIVE).matcher((String) value).matches();
    	}
    	else {
    		value = "";
    	}
    	String hasToggle = (String) component.getAttributes().get("hasToggle");
    	
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
              outRow, justArea, clientId, valueHasRichText, hasToggle); 
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
         String outRow, String justArea, String clientId, boolean valueHasRichText, String hasToggle) throws IOException
  {
	  //come up w/ rows/cols for the textarea if needed
	  int textBoxRows = (new Integer(outRow).intValue()/20);
	  int textBoxCols = (new Integer(outRow).intValue()/3);
	  
	  ResourceLoader rb=new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
    //fck's tool bar can get pretty big
    if (new Integer(outRow).intValue() < 300) 
    {
         outRow = (new Integer(outRow).intValue() + 100) + "";
    }

    //figure out if the toggle should be on
    boolean shouldToggle = ( (hasToggle != null) && (hasToggle.equals("yes")) && !valueHasRichText);
    
    if(shouldToggle)
    {    	
    	//String show_hide_editor = (String) ContextUtil.getLocalizedString(
		//	"org.sakaiproject.tool.assessment.bundle.AuthorMessages", "show_hide_editor");
    	String show_hide_editor = rb.getString("show_hide_editor");
    	writer.write("<div class=\"toggle_link_container\"><a class=\"toggle_link\" id=\"" +clientId+ "_toggle\" href=\"javascript:show_hide_editor('" +  clientId + "');\">" + show_hide_editor + "</a></div>\n");
    }
    else {
        	value = FormattedText.escapeHtmlFormattedTextarea((String) value);
    }
    
    writer.write("<textarea name=\"" + clientId + "_textinput\" id=\"" + clientId + "_textinput\" rows=\""+ textBoxRows + "\" cols=\""+ textBoxCols + "\" class=\"simple_text_area\">");
    writer.write((String) value);
    writer.write("</textarea>");
    if (shouldToggle) {
    	writer.write("<input type=\"hidden\" name=\"" + clientId + "_textinput_current_status\" id=\"" + clientId + "_textinput_current_status\" value=\"firsttime\">");
    }
    else {
    	writer.write("<input type=\"hidden\" name=\"" + clientId + "_textinput_current_status\" id=\"" + clientId + "_textinput_current_status\" value=\"fckonly\">");
    }
    
    writer.write("\n\t<script type=\"text/javascript\" src=\"" + FCK_BASE + FCK_SCRIPT + "\"></script>");

    writer.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
    
    writer.write("\nfunction show_hide_editor(client_id){");
    writer.write("\n\tvar status =  document.getElementById(client_id + '_textinput_current_status');");
    writer.write("\n\tif (status.value == \"firsttime\") {");
    writer.write("\n\t\tstatus.value = \"expaneded\";");
    writer.write("\n\t\tchef_setupformattedtextarea(client_id, true);");
    writer.write("\n\t\tsetBlockDivs();");
    writer.write("\n\t\tretainHideUnhideStatus('none');\n\t}");
    writer.write("\n\telse if (status.value == \"collapsed\") {");
    writer.write("\n\t\tstatus.value = \"expaneded\";");
    writer.write("\n\t\texpandMenu(client_id);\n\t}");
    writer.write("\n\telse if (status.value == \"expaneded\") {");
    writer.write("\n\t\tstatus.value = \"collapsed\";");
    writer.write("\n\t\tcollapseMenu(client_id);\n\t}");    
    writer.write("\n");
    writer.write("\n\tsetMainFrameHeight('Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId().replace("-","x") + "');");
    writer.write("\n}\n");
    
    writer.write("function encodeHTML(text){\n");
    //writer.write("\n\n\talert('encodeHTML');\n");
    writer.write("\ttext = text.replace(\n");
    writer.write("\t\t/&/g, '&amp;').replace(\n");
    writer.write("\t\t/\"/g, '&quot;').replace(\n");
    writer.write("\t\t/</g, '&lt;').replace(\n");
    writer.write("\t\t/>/g, '&gt;');\n");
    writer.write("\treturn text;\n");
    writer.write("}\n");
        
    writer.write("function chef_setupformattedtextarea(client_id,shouldToggle){\n");
    
    writer.write("\tvar textarea_id = client_id + \"_textinput\";\n");   
    //writer.write("\talert('shouldToggle:' + shouldToggle);\n");
    writer.write("\n\tif (shouldToggle == true) {\n");
    //writer.write("\talert('shouldToggle:' + shouldToggle);\n");
    writer.write("\tvar input_text = document.getElementById(textarea_id);\n"); 
    writer.write("\tvar input_text_value = input_text.value;\n"); 
    //writer.write("\talert('before - input_text.value:' + input_text.value);\n");
    writer.write("\tvar input_text_encoded = encodeHTML(input_text_value);\n");
    writer.write("\tinput_text.value = input_text_encoded;\n"); 
    //writer.write("\talert('encoded - input_text.value:' + input_text.value);\n");
    writer.write("\t\n}\n");    
    
    //if toggling is on, hide the toggle when the user goes to richText
    //writer.write("\tif(shouldToggle){\n");
    //writer.write("\t\tvar toggle_id = client_id + \"_toggle\";\n");
    //writer.write("\tvar oToggleDiv = document.getElementById(toggle_id);\n");
    //writer.write("\toToggleDiv.style.display=\"none\";\n");
    //writer.write("\t}\n");

    writer.write("\n\tvar oFCKeditor = new FCKeditor(textarea_id);\n");
    writer.write("\toFCKeditor.BasePath = \"" + FCK_BASE + "\";");
    writer.write("\toFCKeditor.Height = " + outRow + ";");
    writer.write("\n\toFCKeditor.Width = " + outCol + ";");
    //writer.write("\n\n\talert(value':' + oFCKeditor.Value);");
    
    if ( (justArea != null) && (justArea.equals("yes")))
    {
      writer.write("\n\toFCKeditor.ToolbarSet = \"plain\";");
    }
    else
    {

        String connector = "/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector";
        String collectionId = AssessmentService.getContentHostingService().getSiteCollection(ToolManager.getCurrentPlacement().getContext());        

        boolean resourceSearch = EditorConfiguration.enableResourceSearch();
        if(resourceSearch)
        {
        	// need to set document.__pid to placementId
        	String placementId = ToolManager.getCurrentPlacement().getId();
        	writer.write("\tdocument.__pid=\"" + placementId + "\";\n");


        	// need to set document.__baseUrl to baseUrl
        	String baseUrl = ServerConfigurationService.getToolUrl() + "/" + Web.escapeUrl(placementId);
        	writer.write("\tdocument.__baseUrl=\"" + baseUrl + "\";\n");
        }


        writer.write("\n\tvar courseId = \"" + collectionId + "\";"); 
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

        if(resourceSearch)
        {
        	writer.write("\n\toFCKeditor.Config['CustomConfigurationsPath'] = \"/library/editor/FCKeditor/config_rs.js\";\n");
        }
        else
        {
        	writer.write("\n\toFCKeditor.Config['CustomConfigurationsPath'] = \"/library/editor/FCKeditor/config.js\";\n");
        }
    }
    writer.write("\n\tdocument.wysiwyg = \"FCKeditor\";");
    writer.write("\n\n\toFCKeditor.ReplaceTextarea();\n\t}\n");

    writer.write("\nfunction collapseMenu(client_id){");
    writer.write("\n\tvar editor = FCKeditorAPI.GetInstance(client_id + '_textinput');");
    writer.write("\n\teditor.ToolbarSet.Collapse();");
    writer.write("\n\tdocument.wysiwyg = \"textarea\";");
    writer.write("\n}\n");
    
    writer.write("\nfunction expandMenu(client_id){");
    writer.write("\n\tvar editor = FCKeditorAPI.GetInstance(client_id + '_textinput');");
    writer.write("\n\teditor.ToolbarSet.Expand();");
    writer.write("\n\tdocument.wysiwyg = \"FCKeditor\";");
    writer.write("\n}\n");
    
    writer.write("</script>\n");
    
    //if toggling is off or the content is already rich, make the editor show up immediately
    if(!shouldToggle){
    writer.write("<script type=\"text/javascript\" defer=\"1\">chef_setupformattedtextarea('" + clientId + "',false);</script>");
    }    	

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
    String current_status = (String) requestParameterMap.get(clientId + "_textinput_current_status");    
    String finalValue = newValue;
    
    // if use hid the FCK editor, we treat it as text editor
	if ("firsttime".equals(current_status) || "collapsed".equals(current_status)) {
		finalValue = FormattedText.convertPlaintextToFormattedText(newValue);
	}
	else {
		StringBuilder alertMsg = new StringBuilder();
		try
		{
			finalValue = FormattedText.processFormattedText(newValue, alertMsg);
			if (alertMsg.length() > 0)
			{
				log.debug(alertMsg.toString());
			}
		}catch (Exception e)
		{
			log.info(e.getMessage());
		}
	}
	/*
	else {
		boolean valueHasRichText = false;
	    if(newValue != null){
			String valueNoNewLine = ((String) newValue).replaceAll("\\n", "").replaceAll("\\r", "");
			//really simple regex to detect presence of any html tags in the value        
			valueHasRichText = Pattern.compile(".*<.*?>.*", Pattern.CASE_INSENSITIVE).matcher(valueNoNewLine).matches();
	    }
	    // only if user expands the FCK editor, we treat it as rich text
	    if ("expaneded".equals(current_status) && valueHasRichText) {
	    	StringBuilder alertMsg = new StringBuilder();
	    	finalValue = FormattedText.processFormattedText(newValue, alertMsg);
	    	if (alertMsg.length() > 0)
	    	{
	    		log.debug(alertMsg.toString());
	    	}
	    }
	}
	*/
    org.sakaiproject.jsf.component.RichTextEditArea comp = (org.sakaiproject.jsf.component.RichTextEditArea) component;
    comp.setSubmittedValue(finalValue);
  }
}
