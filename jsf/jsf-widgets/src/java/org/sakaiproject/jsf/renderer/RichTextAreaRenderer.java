/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.util.EditorConfiguration;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Web;

public class RichTextAreaRenderer extends Renderer
{
    public boolean supportsComponentType(UIComponent component)
    {
        return (component instanceof org.sakaiproject.jsf.component.RichTextAreaComponent);
    }

    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
        String clientId = component.getClientId(context);
        String textareaId = clientId+"_textarea";

        ResponseWriter writer = context.getResponseWriter();

        // value (text) of the HTMLArea
        Object value = null;
        if (component instanceof UIInput) value = ((UIInput) component).getSubmittedValue();
        if (value == null && component instanceof ValueHolder) value = ((ValueHolder) component).getValue();
        if (value == null) value = "";

        //escape the value so the wysiwyg editors don't get too clever and turn things
        //into tags that are not tags. 
        value = FormattedText.escapeHtmlFormattedTextarea((String) value);

        // pixel width of the HTMLArea
        int width = -1;
        String widthIn = (String) RendererUtil.getAttribute(context, component, "width");
        if (widthIn != null && widthIn.length() > 0) width = Integer.parseInt(widthIn);
        
        // pixel height of the HTMLArea
        int height = -1;
        String heightIn = (String) RendererUtil.getAttribute(context, component, "height");
        if (heightIn != null && heightIn.length() > 0) height = Integer.parseInt(heightIn);
        
        // character height of the textarea
        int columns = -1;
        String columnsStr = (String) RendererUtil.getAttribute(context, component, "columns");
        if (columnsStr != null && columnsStr.length() > 0) columns = Integer.parseInt(columnsStr);
        
        // character width of the textarea
        int rows = -1;
        String rowsStr = (String) RendererUtil.getAttribute(context, component, "rows");
        if (rowsStr != null && rowsStr.length() > 0) rows = Integer.parseInt(rowsStr);
        	
    	ServerConfigurationService serverConfigurationService = (ServerConfigurationService)ComponentManager.get(ServerConfigurationService.class.getName());
        String editor = serverConfigurationService.getString("wysiwyg.editor");
        
        writer.write("<table border=\"0\"><tr><td>");
        writer.write("<textarea name=\"" + textareaId + "\" id=\"" + textareaId + "\"");
        if (columns > 0) writer.write(" cols=\""+columns+"\"");
        if (rows > 0) writer.write(" rows=\""+rows+"\"");
        writer.write(">");
        if (value != null)
           writer.write((String) value);
        writer.write("</textarea>");
        
        writer.write("<script type=\"text/javascript\">sakai.editor.launch('" + textareaId + "');</script>");
        
        //SAK-20818 be sure to close the table
        writer.write("</td></tr></table>\n");
        
        /*
        if(editor!=null && !editor.equalsIgnoreCase("FCKeditor"))
        {

        	// Number of rows of buttons in the toolbar (0, 2, or 3 rows of buttons)
        	int toolbarButtonRows = 2;
        	String toolbarButtonRowsStr = (String) RendererUtil.getAttribute(context, component, "toolbarButtonRows");
        	if (toolbarButtonRowsStr != null && toolbarButtonRowsStr.length() > 0) toolbarButtonRows = Integer.parseInt(toolbarButtonRowsStr);
        	
        	// if true, 0 rows of buttons (no toolbar buttons).
        	String justArea = (String) RendererUtil.getAttribute(context, component, "justArea");
        	if (justArea != null && ("true".equals(justArea) || "yes".equals(justArea)))
        	{
        		toolbarButtonRows = 0;
        	}
        	
        	// the URL to the directory of the HTMLArea JavaScript
        	String javascriptLibrary = "/library/htmlarea";
        	boolean javascriptLibrarySakaiLegacy = true;
        	String newJsLibUrl = (String) RendererUtil.getAttribute(context, component, "javascriptLibrary");
        	{
        		if (newJsLibUrl != null && newJsLibUrl.length() > 0)
        		{
        			javascriptLibrary = newJsLibUrl;
        			javascriptLibrarySakaiLegacy = false;
        		}
        	}
        	
        	// whether to calculate the width, height, and toolbarButtonRows
        	// (instead of just taking the values given on the tag)
        	boolean autoConfig = false;
        	String autoConfigStr = (String) RendererUtil.getAttribute(context, component, "autoConfig");
        	if (autoConfigStr != null && ("true".equals(autoConfigStr) || "yes".equals(autoConfigStr)))
        	{
        		autoConfig = true;
        	}
        	
        	
        	// if necessary, calculate the width, height, and toolbarButtonRows
        	if (autoConfig)
        	{
        		if (toolbarButtonRows == 0)
        		{
        			if (width < 0 && height < 0)
        			{
        				width = 400;
        				height = 100;
        			}
        			else if (height < 0)
        			{
        				height = 200;
        			}
        		}
        		else // if (toolbarButtonRows != 0)
        		{
        			if (width < 0) width = 450;
        			if (height < 0) height = 80;
        			
        			// enforce minimum size of 450 by 80
        			// if width >= 630 set toolbarButtonRows = 2 (otherwise toolbarButtonRows = 3)
        			if ((height < 80) && (width < 450))
        			{
        				height = 80;
        				width = 450;
        				toolbarButtonRows = 3;
        			}
        			else if ((height >= 80) && (width < 450))
        			{
        				width = 450;
        				toolbarButtonRows = 3;
        			}
        			else if ((height >= 80) && (width >= 450) && (width < 630))
        			{
        				toolbarButtonRows = 3;
        			}
        			else if ((height >= 80) && (width >= 630))
        			{
        				toolbarButtonRows = 2;
        			}
        			else if ((height < 80) && (width >= 630))
        			{
        				height = 80;
        				toolbarButtonRows = 2;
        			}
        			else
        			{
        				height = 80;
        				toolbarButtonRows = 3;
        			}
        		}
        		
        	} // if (autoConfig)
        	
        	// surround the HTMLArea in a table to correct the toolbar appearence
        	writer.write("<table border=\"0\"><tr><td>");
        	// output the text area
        	writer.write("<textarea name=\"" + textareaId + "\" id=\"" + textareaId + "\"");
        	if (columns > 0) writer.write(" cols=\""+columns+"\"");
        	if (rows > 0) writer.write(" rows=\""+rows+"\"");
        	//if (toolbarButtonRows == 0) writer.write(" disabled=\"disabled\"");
        	writer.write(">");
        	writer.write((String) value);
        	writer.write("</textarea>");
        	writer.write("</td></tr></table>\n");
        	
        	if (javascriptLibrarySakaiLegacy)
        	{
        		if (width < 0) width = 0;
        		if (height < 0) height = 0;
        		writer.write("<script type=\"text/javascript\" src=\""+javascriptLibrary+"/sakai-htmlarea.js\"></script>\n");
        		writer.write("<script type=\"text/javascript\" defer=\"1\">chef_setupformattedtextarea(\""+textareaId+"\", "+width+", "+height+", "+toolbarButtonRows+");</script>");
        	}
        	else
        	{
        		// output the JavaScript libraries
        		writer.write("<script type=\"text/javascript\">var _editor_url = \""+javascriptLibrary+"/\";</script>\n");
        		writer.write("<script type=\"text/javascript\" src=\""+javascriptLibrary+"/htmlarea.js\"></script>\n");
        		writer.write("<script type=\"text/javascript\" src=\""+javascriptLibrary+"/dialog.js\"></script>\n");
        		writer.write("<script type=\"text/javascript\" src=\""+javascriptLibrary+"/popupwin.js\"></script>\n");
        		writer.write("<script type=\"text/javascript\" src=\""+javascriptLibrary+"/lang/en.js\"></script>\n");
        		
        		// output the JavaScript to configure and invoke the HTMLArea
        		writer.write("<script type=\"text/javascript\">var config=new HTMLArea.Config();");
        		if (toolbarButtonRows == 0)
        		{
        			writer.write("config.toolbar=[];\n");
        		}
        		else if (toolbarButtonRows == 2)
        		{
        */
        			/*
        			 * writer.write(" <textarea name=\"" + clientId +
        			 * "_textarea\" id=\"" + clientId + "_textarea\"" + "
        			 * cols=\"" + outCol + "\" rows=\"" + outRow + "\"
        			 * onclick=\"var config=new HTMLArea.Config();" +
        			 * "config.toolbar = [[\'fontname\', \'space\',\'fontsize\',
        			 * \'space\',\'formatblock\', \'space\',\'bold\',
        			 * \'italic\',
        			 * \'underline\',\'separator\',\'strikethrough\',
        			 * \'subscript\', \'superscript\', \'separator\', \'copy\',
        			 * \'cut\', \'paste\', \'space\', \'undo\', \'redo\']," +
        			 * "[\'separator\', \'justifyleft\', \'justifycenter\',
        			 * \'justifyright\', \'justifyfull\',
        			 * \'separator\',\'outdent\',
        			 * \'indent\',\'separator\',\'forecolor\', \'hilitecolor\',
        			 * \'textindicator\',
        			 * \'separator\',\'inserthorizontalrule\', \'createlink\',
        			 * \'insertimage\', \'inserttable\', \'htmlmode\',
        			 * \'separator\',\'popupeditor\', \'separator\',
        			 * \'showhelp\', \'about\' ],"+ "];" + "HTMLArea.replace(\'" +
        			 * clientId +"_textarea\',config);" + "\">" + value + "
        			 * </textarea>\n");
        			 */
        /*
        			writer.write("config.toolbar = [[\'fontname\', \'space\',\'fontsize\', \'space\',\'forecolor\', \'space\',\'bold\', \'italic\', \'underline\',\'separator\',\'strikethrough\', \'subscript\', \'superscript\', \'separator\',");
        			writer.write("\'justifyleft\', \'justifycenter\', \'justifyright\', \'justifyfull\', \'separator\'],[\'orderedlist\', \'unorderedlist\',\'outdent\', \'indent\',\'htmlmode\', \'separator\', \'showhelp\', \'about\' ],");
        			writer.write("];\n");
        		}
        		else if (toolbarButtonRows == 3)
        		{
        */
        			/*
        			 * writer.write(" <textarea name=\"" + clientId +
        			 * "_textarea\" id=\"" + clientId + "_textarea\"" + "
        			 * cols=\"" + outCol + "\" rows=\"" + outRow + "\"
        			 * onclick=\"var config=new HTMLArea.Config();" +
        			 * "config.toolbar = [[\'fontname\', \'space\',\'fontsize\',
        			 * \'space\',\'formatblock\', \'space\',\'bold\',
        			 * \'italic\', \'underline\']," +
        			 * "[\'separator\',\'strikethrough\', \'subscript\',
        			 * \'superscript\', \'separator\', \'copy\', \'cut\',
        			 * \'paste\', \'space\', \'undo\', \'redo\', \'separator\',
        			 * \'justifyleft\', \'justifycenter\', \'justifyright\',
        			 * \'justifyfull\', \'separator\',\'outdent\', \'indent\']," +
        			 * "[\'separator\',\'forecolor\', \'hilitecolor\',
        			 * \'textindicator\',
        			 * \'separator\',\'inserthorizontalrule\', \'createlink\',
        			 * \'insertimage\', \'inserttable\', \'htmlmode\',
        			 * \'separator\',\'popupeditor\', \'separator\',
        			 * \'showhelp\', \'about\' ],"+ "];" + "HTMLArea.replace(\'" +
        			 * clientId +"_textarea\',config);" + "\">" + value + "
        			 * </textarea>\n");
        			 */
        /*
        			writer.write("config.toolbar = [[\'fontname\', \'space\',\'fontsize\', \'space\',\'formatblock\', \'space\',\'bold\', \'italic\', \'underline\'],");
        			writer.write("[\'separator\',\'strikethrough\', \'subscript\', \'superscript\', \'separator\', \'copy\', \'cut\', \'paste\', \'space\', \'undo\', \'redo\', \'separator\', \'justifyleft\', \'justifycenter\', \'justifyright\', \'justifyfull\', \'separator\',\'outdent\', \'indent\'],");
        			writer.write("[\'separator\',\'forecolor\', \'hilitecolor\', \'textindicator\', \'separator\',\'inserthorizontalrule\', \'createlink\', \'insertimage\', \'inserttable\', \'htmlmode\', \'separator\',\'popupeditor\', \'separator\', \'showhelp\', \'about\' ],");
        			writer.write("];\n");
        		}
        		
        		if (width > 0)
        		{
        			writer.write("config.width=\'");
        			writer.write(String.valueOf(width));
        			writer.write("px");
        			writer.write("\';\n");
        		}
        		if (height > 0)
        		{
        			writer.write("config.height=\'");
        			writer.write(String.valueOf(height));
        			writer.write("px");
        			writer.write("\';\n");
        		}
        		
        		//writer.write("HTMLArea.replace(\'");
        		//writer.write(clientId);
        		//writer.write("_textarea\',config);\n</script>\n");
        		
        		//Jira bug#:SAK-126 returning focus back to first editable field on the web  page
        		writer.write("var ta = HTMLArea.getElementById(\"textarea\",\"");
        		writer.write(clientId);
        		writer.write("_textarea\");\n");
        		writer.write("var editor =new HTMLArea(ta, config);\n");
        		writer.write("setTimeout(	function(){ \n");
        		writer.write("editor.generate(); \n");
        		//somehow the path: messes up to clear status bar:
        		writer.write("editor._statusBar.innerHTML= \' \'; \n");
        		writer.write("editor._statusBar.appendChild(document.createTextNode(HTMLArea.I18N.msg[\"Path\"] + \": \")); \n");
        		//writer.write("editor._iframe.style.border = \"1px solid #000000\";\n");
        		//writer.write("editor._iframe.style.height= \"") ;
        		//writer.write(String.valueOf(height));
        		//writer.write("px\";\n");
        		writer.write("if (document.forms.length > 0)\n");
        		writer.write("{\n");
        		writer.write("var allElements = document.forms[0];\n");
        		writer.write("for (i = 0; i < allElements.length; i++) \n");
        		writer.write("{\n");
        		writer
						.write("if((allElements.elements[i].getAttribute(\"type\") !=null) &&((allElements.elements[i].type == \"text\") || (allElements.elements[i].type == \"textarea\")))\n");
        		writer.write("{\n");
        		writer.write("document.forms[0].elements[i].focus();\n");
        		writer.write("break;\n");
        		writer.write("}\n");
        		writer.write("}\n");
        		writer.write(" }\n");
        		writer.write("}, 400); </script>\n");
        	}
        	//I saw sakai-htmlarea.js already has code to handle focus issue, but it did not work when I tested sakai:rich_text_area tag in jsp's
        	// Jira bug#:SAK-126 returning focus back to first editable field on the web  page
        	writer.write("<script type=\"text/javascript\">\n");
        	writer.write("setTimeout(function(){ \n");
        	writer.write("if (document.forms.length > 0)\n");
        	writer.write("{\n");
        	writer.write("var allElements = document.forms[0];\n");
        	writer.write("for (i = 0; i < allElements.length; i++) \n");
        	writer.write("{\n");
        	writer
					.write("if((allElements.elements[i].getAttribute(\"type\") !=null) &&((allElements.elements[i].type == \"text\") || (allElements.elements[i].type == \"textarea\")))\n");
        	writer.write("{\n");
        	writer.write("document.forms[0].elements[i].focus();\n");
        	writer.write("break;\n");
        	writer.write("}\n");
        	writer.write("}\n");
        	writer.write(" }\n");
        	writer.write("}, 600); </script>\n");
        }
        else
        {
        	ToolManager toolManager = (ToolManager)ComponentManager.get(ToolManager.class.getName());
        	ContentHostingService contentHostingService = (ContentHostingService)ComponentManager.get(ContentHostingService.class.getName());
        	String collectionId = contentHostingService.getSiteCollection(toolManager.getCurrentPlacement().getContext());

        	//is there a slicker way to get this? 
        	String connector = "/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector";

        	writer.write("<table border=\"0\"><tr><td>");
        	writer.write("<textarea name=\"" + textareaId + "\" id=\"" + textareaId + "\"");
        	if (columns > 0) writer.write(" cols=\""+columns+"\"");
        	if (rows > 0) writer.write(" rows=\""+rows+"\"");
        	writer.write(">");
        	writer.write((String) value);
        	writer.write("</textarea>");

        	RendererUtil.writeExternalJSDependencies(context, writer, "richtextarea.jsf.fckeditor.js", "/library/editor/FCKeditor/fckeditor.js");
        	//writer.write("<script type=\"text/javascript\" src=\"/library/editor/FCKeditor/fckeditor.js\"></script>\n");
        	writer.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
        	writer.write("function chef_setupformattedtextarea(textarea_id){\n");
        	writer.write("var oFCKeditor = new FCKeditor(textarea_id);\n");
        	writer.write("oFCKeditor.BasePath = \"/library/editor/FCKeditor/\";\n");

        	if (width < 0) 
        		width = 600;
        	if (height < 0) 
        		height = 400;

        	writer.write("oFCKeditor.Width  = \"" + width + "\" ;\n");
        	writer.write("oFCKeditor.Height = \"" + height + "\" ;\n");

      		writer.write("\n\t\tvar courseId = \"" + collectionId  + "\";");
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

      		boolean resourceSearch = EditorConfiguration.enableResourceSearch();
      		if(resourceSearch)
      		{
      			// need to set document.__pid to placementId
      			String placementId = toolManager.getCurrentPlacement().getId();
      			writer.write("\t\tdocument.__pid=\"" + placementId + "\";\n");

      			// need to set document.__baseUrl to baseUrl
      			String baseUrl = serverConfigurationService.getToolUrl() + "/" + Web.escapeUrl(placementId);
      			writer.write("\t\tdocument.__baseUrl=\"" + baseUrl + "\";\n");
      			writer.write("\n\toFCKeditor.Config['CustomConfigurationsPath'] = \"/library/editor/FCKeditor/config_rs.js\";\n");
      		}
      		else
      		{
      			writer.write("\n\toFCKeditor.Config['CustomConfigurationsPath'] = \"/library/editor/FCKeditor/config.js\";\n");
      		}
 
        	writer.write("oFCKeditor.ReplaceTextarea() ;}\n");
        	writer.write("</script>\n");
        	writer.write("<script type=\"text/javascript\" defer=\"1\">chef_setupformattedtextarea('"+textareaId+"');</script>");

        	writer.write("</td></tr></table>\n");
        }
        */
    }


    public void decode(FacesContext context, UIComponent component)
    {
        if (null == context || null == component
                || !(component instanceof org.sakaiproject.jsf.component.RichTextAreaComponent))
        {
            throw new IllegalArgumentException();
        }

        String clientId = component.getClientId(context);

        Map requestParameterMap = context.getExternalContext()
                .getRequestParameterMap();

        String newValue = (String) requestParameterMap.get(clientId + "_textarea");

        org.sakaiproject.jsf.component.RichTextAreaComponent comp = (org.sakaiproject.jsf.component.RichTextAreaComponent) component;
        comp.setSubmittedValue(newValue);
    }
}


